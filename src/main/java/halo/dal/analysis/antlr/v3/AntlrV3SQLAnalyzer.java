package halo.dal.analysis.antlr.v3;

import halo.dal.analysis.ColumnExper;
import halo.dal.analysis.ParsedTableInfo;
import halo.dal.analysis.SQLAnalyzer;
import halo.dal.analysis.SQLExpression;
import halo.dal.analysis.SQLInfo;
import halo.dal.analysis.SQLStruct;
import halo.dal.analysis.antlr.AntlrParserDelegate;
import halo.dal.analysis.antlr.ColExpr;
import halo.dal.analysis.antlr.DefAntlrParserDelegate;
import halo.dal.analysis.antlr.SQLInfoImpl;
import halo.dal.analysis.antlr.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.apache.commons.lang3.StringUtils;

public class AntlrV3SQLAnalyzer implements SQLAnalyzer {

	protected final static String SQL_BLANK = " ";

	public SQLInfo analyse(String sql, SQLStruct sqlStruct, Object[] values,
			Map<String, Object> context) {
		SQLInfoImpl info = new SQLInfoImpl();
		SQLExpression sqlExpression;
		int i = 0;
		for (ColumnExper o : sqlStruct.getColumnExpers()) {
			sqlExpression = new SQLExpression();
			sqlExpression.setColumn(o.getColumn());
			sqlExpression.setSqlExpressionSymbol(o.getSqlExpressionSymbol());
			sqlExpression.setValue(values[i]);
			info.addSQLExpression(o.getLogicTableName(), sqlExpression);
			i++;
		}
		info.setSQLType(sqlStruct.getSqlType());
		return info;
	}

	public SQLStruct parse(String sql, Map<String, Object> context) {
		AntlrV3SQLLexer lexer = new AntlrV3SQLLexer(new ANTLRStringStream(sql));
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		AntlrV3SQLParser parser = new AntlrV3SQLParser(tokens);
		AntlrParserDelegate delegate = new DefAntlrParserDelegate();
		parser.setAntlrParserDelegate(delegate);
		try {
			parser.start();
		}
		catch (RecognitionException e) {
			throw new RuntimeException(e);
		}
		SQLStruct sqlStruct = new SQLStruct();
		if (!delegate.isHasTable()) {
			sqlStruct.setCanParse(false);
			return sqlStruct;
		}
		sqlStruct.setCanParse(true);
		Table table;
		for (int i = 0; i < delegate.getTables().size(); i++) {
			table = delegate.getTables().get(i);
			sqlStruct.addTable(table.getName(), table.getAlias());
		}
		ColumnExper columnExper;
		for (ColExpr colExpr : delegate.getColExprs()) {
			columnExper = new ColumnExper();
			columnExper.setColumn(colExpr.getColumn());
			columnExper.setSqlExpressionSymbol(colExpr.getOp());
			sqlStruct.addColumnExper(columnExper);
		}
		//设置sql操作类型
		sqlStruct.setSqlType(delegate.getSqlOp());
		return sqlStruct;
	}

	public String outPutSQL(String sql, SQLStruct sqlStruct, SQLInfo sqlInfo,
			ParsedTableInfo parsedTableInfo) {
		List<String> list = new ArrayList<String>();
		List<String> newList = new ArrayList<String>();
		String realTableName;
		for (String tableName : sqlStruct.getTableNames()) {
			realTableName = parsedTableInfo.getRealTable(tableName);
			if (realTableName != null) {
				String alias = sqlStruct.getAliasByTableName(tableName);
				boolean isSame = alias != null && alias.endsWith(tableName);
				if (!isSame) {
					list.add(SQL_BLANK + tableName + ".");
					newList.add(SQL_BLANK + realTableName + ".");
				}
				list.add(SQL_BLANK + tableName + SQL_BLANK);
				newList.add(SQL_BLANK + realTableName + SQL_BLANK);
				list.add(SQL_BLANK + tableName + "(");
				newList.add(" " + realTableName + "(");
				list.add("," + tableName + SQL_BLANK);
				newList.add("," + realTableName + SQL_BLANK);
			}
		}
		String fmtSql = sql.replaceAll("\\. {1,}", "\\.").trim();
		String _sql = StringUtils.replaceEach(fmtSql,
				list.toArray(new String[list.size()]),
				newList.toArray(new String[newList.size()]));
		// 解决sql结束字符串为表名，无法解析的问题例如 delete form user
		String str;
		for (String tableName : sqlStruct.getTableNames()) {
			realTableName = parsedTableInfo.getRealTable(tableName);
			if (realTableName != null) {
				str = SQL_BLANK + tableName;
				int idx = _sql.lastIndexOf(str);
				if (idx == -1) {
					continue;
				}
				if (_sql.substring(idx).equals(str)) {
					_sql = _sql.substring(0, idx) + SQL_BLANK + realTableName;
				}
			}
		}
		return _sql;
	}

	public static void main(String[] args) {
		AntlrV3SQLAnalyzer antlrV3SQLAnalyzer=new AntlrV3SQLAnalyzer();
		String sql="insert into table ( id ,name ) values ( ? ,?) ";
		String sql1="update table set id = ? ,name=? where cnt = ?";
		String sql2="delete * from table  where cnt = ?";
		String sql3="select  * from table  where cnt = ?";
		String sql4="update  user set 1 =1  where uid=? and (age>=? or age<=?) and (sex between ? and ?)  and time<=sysdate()";
		String sql5=  "select gatewayeve0_.ID as ID1_, gatewayeve0_.ADAPTER_ID as ADAPTER2_1_, "
				+ "gatewayeve0_.ADAPTER_MEMO as ADAPTER3_1_, gatewayeve0_.ADAPTER_NAME as ADAPTER4_1_, "
				+ "gatewayeve0_.CREATETIME as CREATETIME1_, gatewayeve0_.END_DATE as END6_1_, "
				+ "gatewayeve0_.EVENT_ID as EVENT7_1_, gatewayeve0_.EVENT_STATUS as EVENT8_1_, "
				+ "gatewayeve0_.EVENT_TYPE as EVENT9_1_, gatewayeve0_.LASTUPDTIME as LASTUPD10_1_, "
				+ "gatewayeve0_.MERCHANT_ID as MERCHANT11_1_, gatewayeve0_.MERCHANT_NAME as MERCHANT12_1_, "
				+ "gatewayeve0_.NAME as NAME1_, gatewayeve0_.OPRID as OPRID1_, "
				+ "gatewayeve0_.START_DATE as START15_1_ "
				+ "from gateway_event gatewayeve0_ "
				+ "where 1=1 and gatewayeve0_.EVENT_STATUS=?";
		SQLStruct sqlStruct=antlrV3SQLAnalyzer.parse(sql4,null);
		System.out.println(sqlStruct);
		System.out.println(sqlStruct.getColumnExpers().size());
	}
}
