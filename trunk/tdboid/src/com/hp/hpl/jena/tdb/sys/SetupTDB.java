/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.sys ;

import static com.hp.hpl.jena.tdb.TDB.logExec ;
import static com.hp.hpl.jena.tdb.TDB.logInfo ;
import static com.hp.hpl.jena.tdb.sys.SystemTDB.BlockReadCacheSize ;
import static com.hp.hpl.jena.tdb.sys.SystemTDB.BlockWriteCacheSize ;
import static com.hp.hpl.jena.tdb.sys.SystemTDB.LenIndexQuadRecord ;
import static com.hp.hpl.jena.tdb.sys.SystemTDB.LenIndexTripleRecord ;
import static com.hp.hpl.jena.tdb.sys.SystemTDB.LenNodeHash ;
import static com.hp.hpl.jena.tdb.sys.SystemTDB.Node2NodeIdCacheSize ;
import static com.hp.hpl.jena.tdb.sys.SystemTDB.NodeId2NodeCacheSize ;
import static com.hp.hpl.jena.tdb.sys.SystemTDB.SizeOfNodeId ;
import static com.hp.hpl.jena.tdb.sys.SystemTDB.SyncTick ;

import java.io.IOException ;
import java.util.Properties ;

import org.openjena.atlas.lib.ColumnMap ;
import org.openjena.atlas.lib.FileOps ;
import org.openjena.atlas.lib.PropertyUtils ;
import org.openjena.atlas.lib.StrUtils ;
import org.slf4j.Logger ;

import com.hp.hpl.jena.sparql.sse.SSEParseException ;
import com.hp.hpl.jena.sparql.util.Utils ;
import com.hp.hpl.jena.tdb.TDB ;
import com.hp.hpl.jena.tdb.TDBException ;
import com.hp.hpl.jena.tdb.base.block.BlockMgr ;
import com.hp.hpl.jena.tdb.base.block.BlockMgrFactory ;
import com.hp.hpl.jena.tdb.base.file.FileFactory ;
import com.hp.hpl.jena.tdb.base.file.FileSet ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.base.file.MetaFile ;
import com.hp.hpl.jena.tdb.base.objectfile.ObjectFile ;
import com.hp.hpl.jena.tdb.base.record.RecordFactory ;
import com.hp.hpl.jena.tdb.index.Index ;
import com.hp.hpl.jena.tdb.index.IndexBuilder ;
import com.hp.hpl.jena.tdb.index.RangeIndex ;
import com.hp.hpl.jena.tdb.index.TupleIndex ;
import com.hp.hpl.jena.tdb.index.TupleIndexRecord ;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTree ;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTreeParams ;
import com.hp.hpl.jena.tdb.migrate.DatasetPrefixStorage ;
import com.hp.hpl.jena.tdb.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.nodetable.NodeTableNative ;
import com.hp.hpl.jena.tdb.nodetable.NodeTableCache ;
import com.hp.hpl.jena.tdb.nodetable.NodeTableFactory ;
import com.hp.hpl.jena.tdb.nodetable.NodeTableInline ;
import com.hp.hpl.jena.tdb.solver.reorder.ReorderLib ;
import com.hp.hpl.jena.tdb.solver.reorder.ReorderTransformation ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.store.DatasetPrefixesTDB ;
import com.hp.hpl.jena.tdb.store.NodeId ;
import com.hp.hpl.jena.tdb.store.QuadTable ;
import com.hp.hpl.jena.tdb.store.TripleTable ;

/** Makes things: datasets from locations, indexes */

public class SetupTDB
{
    //private static final Logger log = LoggerFactory.getLogger(NewSetup.class) ;
    static final Logger log = TDB.logInfo ;
    
    /* Logical information goes in the location metafile. This includes
     * dataset type, NodeTable type and indexes expected.  But it does
     * not include how the particular files are realised.
     * 
     * A NodeTable is a pair of id->Node and Node->id mappings. 
     * 
     * An index file has it's own .meta file saying that it is a B+tree and
     * the record size - everything needed to access it to build a RangeIndex.
     * The individual node table files are the same.  This means we can
     * open a single index or object file (e.g to dump) and it allows
     * for changes both in implementation technology and in overall design. 
     */
    
    // Naming of statics: Maker at a place: X makeX(FileSet, MetaFile?, defaultBlockSize, defaultRecordFactory,
    
    // IndexBuilder for metadata files. 
    
    // TODO Tests.
    // TODO remove constructors (e.g. DatasetPrefixesTDB) that encapsulate the choices).  DI!
    // TODO Check everywhere else for non-DI constructors.
    
    // Old code:
    // 
    // IndexBuilders.  Or add a new IndexBuilder that can make from meta files.
    
    static public final String NodeTableType   = "dat" ; 
    static public final String NodeTableLayout = "1" ;
    
    
    /**  The JVM-wide parameters (these can change without a change to on-disk structure) */ 
    public final static Properties globalConfig = new Properties() ;
    static {
        globalConfig.setProperty(Names.pNode2NodeIdCacheSize,  Integer.toString(Node2NodeIdCacheSize)) ;
        globalConfig.setProperty(Names.pNodeId2NodeCacheSize,  Integer.toString(NodeId2NodeCacheSize)) ;
        globalConfig.setProperty(Names.pBlockWriteCacheSize,   Integer.toString(BlockWriteCacheSize)) ;
        globalConfig.setProperty(Names.pBlockReadCacheSize,    Integer.toString(BlockReadCacheSize)) ;
        globalConfig.setProperty(Names.pSyncTick,              Integer.toString(SyncTick)) ;
    }

    // And here we make datasets ... 
    public static DatasetGraphTDB buildDataset(Location location)
    {
        /* ---- this.meta - the logical structure of the dataset.
         * 
         * # Dataset design
         * tdb.create.version=0.9           # TDB version that created this dataset originally.  0.8 for pre-meta.
         * tdb.layout=v1                    # "version" for the design)
         * tdb.type=standalone              # --> nodes-and-triples/quads
         * tdb.created=                     # Informational timestamp of creation.
         * tdb.nodeid.size=8                # Bytes (SizeOfNodeId)
         * tdb.node.hashsize=16             # Bytes (LenNodeHash)
         * tdb.record.triple=24             # LenIndexTripleRecord
         * tdb.record.quad=32               # LenIndexQuadRecord
         * 
         * # Triple table
         * # Changing the indexes does not automatically change the indexing on the dataset.
         * tdb.indexes.triples.primary=SPO  # triple primary
         * tdb.indexes.triples=SPO,POS,OSP  # triple table indexes
         * 
         * # Quad table.
         * tdb.indexes.quads.primary=GSPO   # Quad table primary.
         * tdb.indexes.quads=GSPO,GPOS,GOSP,SPOG,POSG,OSPG  # Quad indexes
         *
         * # Node table.
         * tdb.nodetable.mapping.node2id=node2id
         * tdb.nodetable.mapping.id2node=id2node
         * tdb.nodetable.mapping.data=nodes
         *
         * # Prefixes.
         *
         * tdb.prefixes.index.file=prefixIdx
         * tdb.prefixes.indexes=GPU
         * tdb.prefixes.primary=GPU
         * 
         * tdb.prefixes.nodetable.mapping.node2id=prefixes
         * tdb.prefixes.nodetable.mapping.id2node=id2prefix
         *
         * and then for each file we have the concrete parameters for the file:
         * 
         * ---- An index
         * 
         * tdb.file.type=rangeindex        # Service provided.
         * tdb.file.impl=bplustree         # Implementation
         * tdb.file.impl.version=bplustree-v1          
         * 
         * tdb.index.name=SPO
         * tdb.index.order=SPO
         *
         * tdb.bplustree.record=24,0 or 32,0
         * (tdb.bplustree.order=)
         * tdb.bplustree.blksize=
         * 
         * ---- An object file
         *
         * tdb.file.type=object
         * tdb.file.impl=dat
         * tdb.file.impl.version=dat-v1
         *
         * tdb.object.encoding=sse
         * 
         */ 
        
        // Check and set defaults.
        // On return, can just read the metadata key/value. 
        
        /* Semi-global
         * TODO
         * tdb.cache.node2id.size=100000  # Smaller? Much smaller!
         * tdb.cache.id2node.size=100000
         * tdb.cache.blockwrite.size=2000
         * tdb.cache.blockread.size=10000
         * tdb.synctick=100000
         */
        
        String propertiesFile = "tdb.properties" ; 
        MetaFile metafile = locationMetadata(location) ;
        // dupulicate
        Properties config = new Properties(globalConfig) ;
        boolean localProperties = false ;
        
        if ( location.exists(propertiesFile) )
        {
            // Load now to test for errors.
            localProperties = true ;
            try { PropertyUtils.loadFromFile(config, propertiesFile) ; }
            catch (IOException ex) { throw new TDBException(ex) ; }
        }
        
        // Only support this so far.
        if ( ! metafile.propertyEquals("tdb.layout", "v1") )
            SetupTDB.error(log, "Excepted 'v1': Wrong layout: "+metafile.getProperty("tdb.layout")) ;
            
        if ( ! metafile.propertyEquals("tdb.type", "standalone") )
            SetupTDB.error(log, "Not marked as a standalone type: "+metafile.getProperty("tdb.type")) ;

        // Check expectations.
        
        metafile.checkOrSetMetadata("tdb.nodeid.size", Integer.toString(SizeOfNodeId)) ;
        metafile.checkOrSetMetadata("tdb.node.hashsize", Integer.toString(LenNodeHash)) ;
        
        metafile.checkOrSetMetadata("tdb.record.triple", Integer.toString(LenIndexTripleRecord)) ;
        metafile.checkOrSetMetadata("tdb.record.quad",   Integer.toString(LenIndexQuadRecord)) ;
        
        // ---------------------
        
        // ---- Logical structure

        // -- Node Table.
        
        String indexNode2Id = metafile.getProperty("tdb.nodetable.mapping.node2id") ;
        String indexId2Node = metafile.getProperty("tdb.nodetable.mapping.id2node") ;
        
        String nodesdata = metafile.getProperty("tdb.nodetable.mapping.data") ;
        
        log.debug("Object table: "+indexNode2Id+" - "+indexId2Node) ;
        
        // Cache sizes should come from this.info.
        NodeTable nodeTable = makeNodeTable(location, 
                                            indexNode2Id,
                                            SystemTDB.Node2NodeIdCacheSize,
                                            indexId2Node, SystemTDB.NodeId2NodeCacheSize) ;

        TripleTable tripleTable = makeTripleTable(location, config, nodeTable, 
                                                  Names.primaryIndexTriples, Names.tripleIndexes) ;
        QuadTable quadTable = makeQuadTable(location, config, nodeTable,
                                            Names.primaryIndexQuads, Names.quadIndexes) ;

        DatasetPrefixStorage prefixes = makePrefixes(location, config) ;

        // ---- Create the DatasetGraph object
        DatasetGraphTDB dsg = new DatasetGraphTDB(tripleTable, quadTable, prefixes, chooseOptimizer(location), location, config) ;

        // Finalize
        metafile.flush() ;
        
        // Set TDB features.
        if ( localProperties )
        {
            /*
             * tdb.feature.????
             */

            String base = "tdb.feature." ;
            String key= base+TDB.symUnionDefaultGraph.getSymbol() ;
            
            boolean unionDefaultGraph = PropertyUtils.getPropertyAsBoolean(config, key, false) ;
            if ( unionDefaultGraph )
                dsg.getContext().setTrue(TDB.symUnionDefaultGraph) ;
            
            // Settable on a per-dadaset basis.
            //symUnionDefaultGraph
            //symLogDuplicates
            //symFileMode - later
        }
        
        return dsg ;
    }

    public static TripleTable makeTripleTable(Location location, Properties config, NodeTable nodeTable, String dftPrimary, String[] dftIndexes)
    {
        MetaFile metafile = location.getMetaFile() ;
        String primary = metafile.getOrSetDefault("tdb.indexes.triples.primary", dftPrimary) ;
        String x = metafile.getOrSetDefault("tdb.indexes.triples", StrUtils.strjoin(",",dftIndexes)) ;
        String indexes[] = x.split(",") ;
        
        if ( indexes.length != 3 )
            SetupTDB.error(log, "Wrong number of triple table indexes: "+StrUtils.strjoin(",", indexes)) ;
        log.debug("Triple table: "+primary+" :: "+StrUtils.join(",", indexes)) ;
        
        TupleIndex tripleIndexes[] = makeTupleIndexes(location, config, primary, indexes, indexes) ;
        if ( tripleIndexes.length != indexes.length )
            SetupTDB.error(log, "Wrong number of triple table tuples indexes: "+tripleIndexes.length) ;
        TripleTable tripleTable = new TripleTable(tripleIndexes, nodeTable) ;
        metafile.flush() ;
        return tripleTable ;
    }
    
    public static QuadTable makeQuadTable(Location location, Properties config, NodeTable nodeTable, String dftPrimary, String[] dftIndexes)
    {
        MetaFile metafile = location.getMetaFile() ; 
        String primary = metafile.getOrSetDefault("tdb.indexes.quads.primary", dftPrimary) ;
        String x = metafile.getOrSetDefault("tdb.indexes.quads", StrUtils.strjoin(",",dftIndexes)) ;
        String indexes[] = x.split(",") ;

        if ( indexes.length != 6 )
            SetupTDB.error(log, "Wrong number of quad table indexes: "+StrUtils.strjoin(",", indexes)) ;
        log.debug("Quad table: "+primary+" :: "+StrUtils.join(",", indexes)) ;
        
        TupleIndex quadIndexes[] = makeTupleIndexes(location, config, primary, indexes, indexes) ;
        if ( quadIndexes.length != indexes.length )
            SetupTDB.error(log, "Wrong number of triple table tuples indexes: "+quadIndexes.length) ;
        QuadTable quadTable = new QuadTable(quadIndexes, nodeTable) ;
        metafile.flush() ;
        return quadTable ;
    }


    public static DatasetPrefixStorage makePrefixes(Location location, Properties config)
    {
        /*
         * tdb.prefixes.index.file=prefixIdx
         * tdb.prefixes.indexes=GPU
         * tdb.prefixes.primary=GPU
         * 
         * tdb.prefixes.nodetable.mapping.node2id=prefixes
         * tdb.prefixes.nodetable.mapping.id2node=id2prefix
    
         * 
         * Logical:
         * 
         * tdb.prefixes.index.file=prefixIdx
         * tdb.prefixes.index=GPU
         * tdb.prefixes.nodetable.mapping.node2id=prefixes
         * tdb.prefixes.nodetable.mapping.id2node=id2prefix
    
         * 
         * Physical:
         * 
         * It's a node table and an index (rangeindex)
         * 
         */

        // Some of this is also in locationMetadata.
        
        MetaFile metafile = location.getMetaFile() ;
    
        // The index using for Graph+Prefix => URI
        String indexPrefixes = metafile.getOrSetDefault("tdb.prefixes.index.file", Names.indexPrefix) ;
        String primary = metafile.getOrSetDefault("tdb.prefixes.primary", Names.primaryIndexPrefix) ;
        String x = metafile.getOrSetDefault("tdb.prefixes.indexes", StrUtils.strjoin(",",Names.prefixIndexes)) ;
        String indexes[] = x.split(",") ;
        
        TupleIndex prefixIndexes[] = makeTupleIndexes(location, config, primary, indexes, new String[]{indexPrefixes}) ;
        if ( prefixIndexes.length != indexes.length )
            SetupTDB.error(log, "Wrong number of triple table tuples indexes: "+prefixIndexes.length) ;
        
        // The nodetable.
        String pnNode2Id = metafile.getOrSetDefault("tdb.prefixes.nodetable.mapping.node2id", Names.prefixNode2Id) ;
        String pnId2Node = metafile.getOrSetDefault("tdb.prefixes.nodetable.mapping.id2node", Names.prefixId2Node) ;
        
        // No cache - the prefix mapping is a cache
        NodeTable prefixNodes = makeNodeTable(location, pnNode2Id, -1, pnId2Node, -1)  ;
        
        DatasetPrefixesTDB prefixes = new DatasetPrefixesTDB(prefixIndexes, prefixNodes) ; 
        
        log.debug("Prefixes: "+x) ;
        
        return prefixes ;
    }

    public static TupleIndex[] makeTupleIndexes(Location location, Properties config, String primary, String[] descs, String[] filenames)
    {
        if ( primary.length() != 3 && primary.length() != 4 )
            SetupTDB.error(log, "Bad primary key length: "+primary.length()) ;

        int indexRecordLen = primary.length()*NodeId.SIZE ;
        TupleIndex indexes[] = new TupleIndex[descs.length] ;
        for (int i = 0 ; i < indexes.length ; i++)
            indexes[i] = makeTupleIndex(location, config, primary, descs[i], filenames[i], indexRecordLen) ;
        return indexes ;
    }
    
    public static TupleIndex makeTupleIndex(Location location,
                                            Properties config,
                                            String primary, String indexOrder, String indexName,
                                            int keyLength)
    {
        /*
        * tdb.file.type=rangeindex        # Service provided.
        * tdb.file.impl=bplustree         # Implementation
        * tdb.file.impl.version=bplustree-v1          
        */

        FileSet fs = new FileSet(location, indexName) ;
        // Physical
        MetaFile metafile = fs.getMetaFile() ;
        
        metafile.checkOrSetMetadata("tdb.file.type", "rangeindex") ;
        metafile.checkOrSetMetadata("tdb.file.indexorder", indexOrder) ;
        
        int readCacheSize = PropertyUtils.getPropertyAsInteger(config, Names.pBlockReadCacheSize) ;
        int writeCacheSize = PropertyUtils.getPropertyAsInteger(config, Names.pBlockWriteCacheSize) ;
        
        // Value part is null (zero length)
        RangeIndex rIndex = makeRangeIndex(location, indexName, keyLength, 0, readCacheSize, writeCacheSize) ;
        TupleIndex tupleIndex = new TupleIndexRecord(primary.length(), new ColumnMap(primary, indexOrder), rIndex.getRecordFactory(), rIndex) ;
        metafile.flush() ;
        return tupleIndex ;
    }
    
    public static Index makeIndex(Location location, String indexName, 
                                   int dftKeyLength, int dftValueLength, 
                                   int readCacheSize,int writeCacheSize)
    {
        return makeRangeIndex(location, indexName, dftKeyLength, dftValueLength, readCacheSize, writeCacheSize) ;
    }
    
    public static RangeIndex makeRangeIndex(Location location, String indexName, 
                                             int dftKeyLength, int dftValueLength,
                                             int readCacheSize,int writeCacheSize)
    {
        /*
         * tdb.file.type=rangeindex        # Service provided.
         * tdb.file.impl=bplustree         # Implementation
         * tdb.file.impl.version=bplustree-v1          
         */
         FileSet fs = new FileSet(location, indexName) ;
         // Physical
         MetaFile metafile = fs.getMetaFile() ;
         metafile.checkOrSetMetadata("tdb.file.type", "rangeindex") ;
         String indexType = metafile.getOrSetDefault("tdb.file.impl", "bplustree") ;
         if ( ! indexType.equals("bplustree") )
         {
             log.error("Unknown index type: "+indexType) ;
             throw new TDBException("Unknown index type: "+indexType) ;
         }
         metafile.checkOrSetMetadata("tdb.file.impl.version", "bplustree-v1") ;
         
         RangeIndex rIndex =  makeBPlusTree(fs, readCacheSize, writeCacheSize, dftKeyLength, dftValueLength) ;
         metafile.flush();
         return rIndex ;
    }
    
    public static RangeIndex makeBPlusTree(FileSet fs, 
                                           int readCacheSize, int writeCacheSize,
                                           int dftKeyLength, int dftValueLength)
    {
        // ---- BPlusTree
        // Get parameters.
        /*
         * tdb.bplustree.record=24,0
         * tdb.bplustree.blksize=
         * tdb.bplustree.order=
         */
        
        MetaFile metafile = fs.getMetaFile() ;
        RecordFactory recordFactory = makeRecordFactory(metafile, "tdb.bplustree.record", dftKeyLength, dftValueLength) ;
        
        String blkSizeStr = metafile.getOrSetDefault("tdb.bplustree.blksize", Integer.toString(SystemTDB.BlockSize)) ; 
        int blkSize = SetupTDB.parseInt(blkSizeStr, "Bad block size") ;
        
        // IndexBuilder.getBPlusTree().newRangeIndex(fs, recordFactory) ;
        // Does not set order.
        
        int calcOrder = BPlusTreeParams.calcOrder(blkSize, recordFactory.recordLength()) ;
        String orderStr = metafile.getOrSetDefault("tdb.bplustree.order", Integer.toString(calcOrder)) ;
        int order = SetupTDB.parseInt(orderStr, "Bad order for B+Tree") ;
        if ( order != calcOrder )
            SetupTDB.error(log, "Wrong order (" + order + "), calculated = "+calcOrder) ;

        RangeIndex rIndex = createBPTree(fs, order, blkSize, readCacheSize, writeCacheSize, recordFactory) ;
        metafile.flush() ;
        return rIndex ;
    }

    public static RecordFactory makeRecordFactory(MetaFile metafile, String keyName, int keyLenDft, int valLenDft)
    {
        String recSizeStr = null ;
        
        if ( keyLenDft >= 0 && valLenDft >= 0 )
        {
            String dftRecordStr = keyLenDft+","+valLenDft ;
            recSizeStr = metafile.getOrSetDefault(keyName, dftRecordStr) ;
        }
        else
            recSizeStr = metafile.getProperty(keyName) ;
        
        if ( recSizeStr == null )
            SetupTDB.error(log, "Failed to get a record factory description from "+keyName) ;
        
        
        String[] recordLengths = recSizeStr.split(",") ;
        if ( recordLengths.length != 2 )
            SetupTDB.error(log, "Bad record length: "+recSizeStr) ;

        int keyLen = SetupTDB.parseInt(recordLengths[0], "Bad key length ("+recSizeStr+")") ;
        int valLen = SetupTDB.parseInt(recordLengths[1], "Bad value length ("+recSizeStr+")") ;
        
        return new RecordFactory(keyLen, valLen) ;
    }
    
    /** Make a NodeTable without cache and inline wrappers */ 
    public static NodeTable makeNodeTableBase(Location location, String indexNode2Id, String indexId2Node)
    {
        if (location.isMem()) 
            return NodeTableFactory.createMem(IndexBuilder.mem()) ;

        /* Logical:
         * # Node table.
         * tdb.nodetable.mapping.node2id=node2id
         * tdb.nodetable.mapping.id2node=id2node
         * 
         * Physical:
         * 1- Index file for node2id
         * 2- Cached direct lookup object file for id2node
         *    Encoding. 
         */   
        
        String nodeTableType = location.getMetaFile().getProperty(Names.kNodeTableType) ;

        if (nodeTableType != null)
        {
            if ( ! nodeTableType.equals(NodeTableType))
                log.debug("Explicit node table type: " + nodeTableType + " (ignored)") ;
        }
        else
        {
            location.getMetaFile().setProperty(Names.kNodeTableType, NodeTableType) ;
            location.getMetaFile().setProperty(Names.kNodeTableLayout, NodeTableLayout) ;
        }
        
        // -- make id to node mapping -- Names.indexId2Node
        FileSet fsIdToNode = new FileSet(location, indexId2Node) ;
        //checkMetadata(fsIdToNode.getMetaFile(), /*Names.kNodeTableType,*/ NodeTable.type) ; 
        
        ObjectFile stringFile = makeObjectFile(fsIdToNode) ;
        
        // -- make node to id mapping -- Names.indexNode2Id
        // Make index of id to node (data table)
        
        // No caching at the index level - we use the internal caches of the node table.
        Index nodeToId = makeIndex(location, indexNode2Id, LenNodeHash, SizeOfNodeId, -1 ,-1) ;
        
        // -- Make the node table using the components established above.
        NodeTable nodeTable = new NodeTableNative(nodeToId, stringFile) ;
        return nodeTable ;
    }
    
    /** Make a NodeTable with cache and inline wrappers */ 
    public static NodeTable makeNodeTable(Location location,
                                          String indexNode2Id, int nodeToIdCacheSize,
                                          String indexId2Node, int idToNodeCacheSize)
    {
        NodeTable nodeTable = makeNodeTableBase(location, indexNode2Id, indexId2Node) ;
        nodeTable = NodeTableCache.create(nodeTable, nodeToIdCacheSize, idToNodeCacheSize) ; 
        nodeTable = NodeTableInline.create(nodeTable) ;
        return nodeTable ;
    }

    public static ObjectFile makeObjectFile(FileSet fsIdToNode)
    {
        /* Physical
         * ---- An object file
         * tdb.file.type=object
         * tdb.file.impl=dat
         * tdb.file.impl.version=dat-v1
         *
         * tdb.object.encoding=sse 
         */
        
        MetaFile metafile = fsIdToNode.getMetaFile() ;
        metafile.checkOrSetMetadata("tdb.file.type", ObjectFile.type) ;
        metafile.checkOrSetMetadata("tdb.file.impl", "dat") ;
        metafile.checkOrSetMetadata("tdb.file.impl.version", "dat-v1") ;
        metafile.checkOrSetMetadata("tdb.object.encoding", "sse") ;
        
        String filename = fsIdToNode.filename(Names.extNodeData) ;
        ObjectFile objFile = FileFactory.createObjectFileDisk(filename);
        metafile.flush();
        return objFile ;
    }

    /** Check and set default for the dataset design */
    public static MetaFile locationMetadata(Location location)
    {
        boolean newDataset = location.isMem() || ! FileOps.existsAnyFiles(location.getDirectoryPath()) ; 

        MetaFile metafile = location.getMetaFile() ;
        boolean isPreMetadata = false ;
        
        if (!newDataset && metafile.existsMetaData())
        {
            // Existing metadata
            String verString = metafile.getProperty("tdb.create.version", "unknown") ;
            TDB.logInfo.debug("Location: " + location.toString()) ;
            TDB.logInfo.debug("Version:  " + verString) ;
        }
        else
        {
            // Not new ?, no metadata
            // Either it's brand new (so set the defaults)
            // or it's a pre-0.9 dataset (files exists)

            if ( ! newDataset )
            {
                // Well-known name of the primary triples index.
                isPreMetadata = FileOps.exists(location.getPath("SPO.idn")) ;
                // PROBLEM.
//                boolean b = FileOps.exists(location.getPath("SPO.idn")) ;
//                if ( !b )
//                {
//                    log.error("Existing files but no metadata and not old-style fixed layout: "+location.getDirectoryPath()) ;
//                    File d = new File(location.getDirectoryPath()) ;
//                    File[] entries = d.listFiles() ;
//                    for ( File f : d.listFiles()  )
//                        log.error("File: "+f.getName()) ;
//                    throw new TDBException("Can't build dataset: "+location) ;
//                }
//                isPreMetadata = true ;
            }
        }
            
        // Ensure defaults.
        
        if ( newDataset )
        {
            metafile.ensurePropertySet("tdb.create.version", TDB.VERSION) ;
            metafile.ensurePropertySet("tdb.created", Utils.nowAsXSDDateTimeString()) ;
        }
        
        if ( isPreMetadata )
        {
            // Existing location (has some files in it) but no metadata.
            // Fake it as TDB 0.8.1 (which did not have metafiles)
            // If it's the wrong file format, things do badly wrong later.
            metafile.ensurePropertySet("tdb.create.version", "0.8") ;
            metafile.setProperty(Names.kCreatedDate, Utils.nowAsXSDDateTimeString()) ;
        }
            
        metafile.ensurePropertySet("tdb.layout", "v1") ;
        metafile.ensurePropertySet("tdb.type", "standalone") ;
        
        String layout = metafile.getProperty("tdb.layout") ;
        
        if ( layout.equals("v1") )
        {
            metafile.ensurePropertySet("tdb.indexes.triples.primary", Names.primaryIndexTriples) ;
            metafile.ensurePropertySet("tdb.indexes.triples", StrUtils.join(",", Names.tripleIndexes)) ;

            metafile.ensurePropertySet("tdb.indexes.quads.primary", Names.primaryIndexQuads) ;
            metafile.ensurePropertySet("tdb.indexes.quads", StrUtils.join(",", Names.quadIndexes)) ;
            
            metafile.ensurePropertySet("tdb.nodetable.mapping.node2id", Names.indexNode2Id) ;
            metafile.ensurePropertySet("tdb.nodetable.mapping.id2node", Names.indexId2Node) ;
            
            metafile.ensurePropertySet("tdb.prefixes.index.file", Names.indexPrefix) ;
            metafile.ensurePropertySet("tdb.prefixes.nodetable.mapping.node2id", Names.prefixNode2Id) ;
            metafile.ensurePropertySet("tdb.prefixes.nodetable.mapping.id2node", Names.prefixId2Node) ;
            
        }
        else
            SetupTDB.error(log, "tdb.layout: expected v1") ;
            
        
        metafile.flush() ;
        return metafile ; 
    }

//    public static Index createIndex(FileSet fileset, RecordFactory recordFactory)
//    {
//        return chooseIndexBuilder(fileset).newIndex(fileset, recordFactory) ;
//    }
//    
//    public static RangeIndex createRangeIndex(FileSet fileset, RecordFactory recordFactory)
//    {
//        // Block size control?
//        return chooseIndexBuilder(fileset).newRangeIndex(fileset, recordFactory) ;
//    }

    /** Knowing all the parameters, create a B+Tree */
    public static RangeIndex createBPTree(FileSet fileset, int order, 
                                          int blockSize,
                                          int readCacheSize, int writeCacheSize,
                                          RecordFactory factory)
    {
        // ---- Checking
        if (blockSize < 0 && order < 0) throw new IllegalArgumentException("Neither blocksize nor order specified") ;
        if (blockSize >= 0 && order < 0) order = BPlusTreeParams.calcOrder(blockSize, factory.recordLength()) ;
        if (blockSize >= 0 && order >= 0)
        {
            int order2 = BPlusTreeParams.calcOrder(blockSize, factory.recordLength()) ;
            if (order != order2) throw new IllegalArgumentException("Wrong order (" + order + "), calculated = "
                                                                    + order2) ;
        }
    
        // Iffy - does not allow for slop.
        if (blockSize < 0 && order >= 0)
        {
            // Only in-memory.
            blockSize = BPlusTreeParams.calcBlockSize(order, factory) ;
        }
    
        BPlusTreeParams params = new BPlusTreeParams(order, factory) ;
        BlockMgr blkMgrNodes = BlockMgrFactory.create(fileset, Names.bptExt1, blockSize, readCacheSize, writeCacheSize) ;
        BlockMgr blkMgrRecords = BlockMgrFactory.create(fileset, Names.bptExt2, blockSize, readCacheSize, writeCacheSize) ;
        return BPlusTree.attach(params, blkMgrNodes, blkMgrRecords) ;
    }

    public static ReorderTransformation chooseOptimizer(Location location)
    {
        if ( location == null )
            return ReorderLib.identity() ;
        
        ReorderTransformation reorder = null ;
        if ( location.exists(Names.optStats) )
        {
            try {
                reorder = ReorderLib.weighted(location.getPath(Names.optStats)) ;
                logInfo.info("Statistics-based BGP optimizer") ;  
            } catch (SSEParseException ex) { 
                log.warn("Error in stats file: "+ex.getMessage()) ;
                reorder = null ;
            }
        }
        
        if ( reorder == null && location.exists(Names.optFixed) )
        {
            // Not as good but better than nothing.
            reorder = ReorderLib.fixed() ;
            logInfo.info("Fixed pattern BGP optimizer") ;  
        }
        
        if ( location.exists(Names.optNone) )
        {
            reorder = ReorderLib.identity() ;
            logInfo.info("Optimizer explicitly turned off") ;
        }
    
        if ( reorder == null )
            reorder = SystemTDB.defaultOptimizer ;
        
        if ( reorder == null )
            logExec.warn("No BGP optimizer") ;
        
        return reorder ; 
    }

    public static void error(Logger log, String msg)
    {
        if ( log != null )
            log.error(msg) ;
        throw new TDBException(msg) ;
    }

    public static int parseInt(String str, String messageBase)
    {
        try { return Integer.parseInt(str) ; }
        catch (NumberFormatException ex) { error(log, messageBase+": "+str) ; return -1 ; }
    }
}
/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP All rights
 * reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. The name of the author may not
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */