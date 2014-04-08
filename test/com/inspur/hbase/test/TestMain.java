package com.inspur.hbase.test;

import java.util.Date;
import java.util.List;
import java.util.Random;

import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.hadoop.hbase.util.Bytes;

import com.inspur.hbase.respository.HBaseSolrRespository;
import com.inspur.hbase.schema.Page;
import com.inspur.hbase.service.BaseService;
import com.inspur.hbase.service.BaseServiceImpl;

public class TestMain {

	public static void main(String[] args) {

		try {

			BaseService<Case> caseService = new BaseServiceImpl<Case>(Case.class, new HBaseSolrRespository("http://192.168.1.100:8080/search/HBaseData"));

			// create or delete table
			caseService.deleteTable();
			caseService.createTableIFNotExists();

			// create random data
			String[] codeArray = new String[] { "001", "001001", "001002", "003", "003001", "003002", "002", "002001", "002002" };
			String[] type1Array = new String[] { "0", "1", "2" };
			String[] type2Array = new String[] { "A", "B" };
			String[] type3Array = new String[] { "T", "F" };
			for (int i = 0; i < 200; i++) {
				Float rand = new Random().nextFloat() * 10;
				Case insert = new Case();
				insert.setId(String.valueOf(new Date().getTime()));
				insert.setCode(codeArray[rand.intValue() % 9]);
				insert.setType1(type1Array[rand.intValue() % 3]);
				insert.setType2(type2Array[rand.intValue() % 2]);
				insert.setType3(type3Array[rand.intValue() % 2]);
				caseService.saveBoth(insert);
			}

			// delete data from hbase and solr, need real id for replacing "1396963284853"
			caseService.deleteByIdBoth("1396963284853");

			// update for hbase and solr, need real id for replacing "1396963420784"
			Case update = new Case();
			update.setId("1396963420784");
			update.setCode("024");
			update.setType1("2");
			update.setType2("A");
			update.setType3("T");
			update.setDate(new Date().toString());
			caseService.updateBoth(update);

			// find data by id, need real id for replacing "1396963420784"
			Case caseTemp = caseService.findById("1396963420784");
			System.out.println(caseTemp.toString());

			// find all data with start and stop, need real id for replacing "1396963418104" and "1396963421392"
			List<Case> caseList = caseService.findAll(Bytes.toBytes("1396963418104"), Bytes.toBytes("1396963421392"));
			for (Case caseStat : caseList) {
				System.out.println(caseStat.toString());
			}

			// find by solr
			List<Case> caseList1 = caseService.findBySolr(new String[] { "code", "type2", "type3" }, new String[] { "code:[002 TO 003}", "type3:T" }, null, null);
			for (Case caseStat : caseList1) {
				System.out.println(caseStat.toString());
			}

			// find by hbase with custom filter, need real id for replacing "1396963418742" and "1396963418104" and "1396963421392"
			List<Case> caseList2 = caseService.findByFilter(new RowFilter(CompareFilter.CompareOp.LESS_OR_EQUAL, new BinaryComparator(Bytes.toBytes("1396963418742"))), Bytes.toBytes("1396963418104"), Bytes.toBytes("1396963421392"));
			for (Case caseStat : caseList2) {
				System.out.println(caseStat.toString());
			}

			// page by solr
			Page<Case> page = new Page<Case>();
			page = caseService.pageBySolr(new String[] { "code", "type2", "type3" }, new String[] { "code:[002 TO 003}" }, null, null, page);
			System.out.println(page.getCurrent());
			for (Case caseStat : page.getDataList()) {
				System.out.println(caseStat.toString());
			}
			for (int i = 1; i < page.getTotal(); i++) {
				page.setCurrent(page.getCurrent() + 1);
				page = caseService.pageBySolr(new String[] { "code", "type2", "type3" }, new String[] { "code:[002 TO 003}" }, null, null, page);
				System.out.println(page.getCurrent());
				for (Case caseStat : page.getDataList()) {
					System.out.println(caseStat.toString());
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
