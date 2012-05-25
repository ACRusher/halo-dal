package halo.dal;

import halo.dal.analysis.DefPartitionParserFactory;
import halo.dal.analysis.PartitionParserFactory;
import halo.dal.analysis.SQLAnalyzer;
import halo.dal.analysis.def.DefSQLAnalyzer;

/**
 * DAL各种服务的赋值
 * 
 * @author akwei
 */
public class DALFactory {

    private static DALFactory ins = new DALFactory();

    public static DALFactory getDefault() {
        return ins;
    }

    private SQLAnalyzer sqlAnalyzer;

    private PartitionParserFactory partitionParserFactory;

    public DALFactory() {
        ins = this;
        this.setSqlAnalyzer(new DefSQLAnalyzer());
        this.setPartitionParserFactory(new DefPartitionParserFactory());
    }

    public DALFactory(SQLAnalyzer sqlAnalyzer,
            PartitionParserFactory dalDefPartitionParserFactory) {
        super();
        ins = this;
        this.sqlAnalyzer = sqlAnalyzer;
        this.partitionParserFactory = dalDefPartitionParserFactory;
    }

    public void setSqlAnalyzer(SQLAnalyzer sqlAnalyzer) {
        this.sqlAnalyzer = sqlAnalyzer;
    }

    public SQLAnalyzer getSqlAnalyzer() {
        return sqlAnalyzer;
    }

    public void setPartitionParserFactory(
            PartitionParserFactory partitionParserFactory) {
        this.partitionParserFactory = partitionParserFactory;
    }

    public PartitionParserFactory getPartitionParserFactory() {
        return partitionParserFactory;
    }
}