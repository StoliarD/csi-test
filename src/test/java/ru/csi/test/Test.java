package ru.csi.test;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.Assert;
import ru.csi.test.entities.Data;
import ru.csi.test.hbn_entities.Item;
import ru.csi.test.hbn_entities.Price;

import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Test {

	private static final String DATE_PATTERN = "dd.MM.yyyy hh:mm:ss";

	private static final String S1 = "122856 1 1 01.01.2013 00:00:00 31.01.2013 23:59:59 11000";
	private static final String S2 = "122856 2 1 10.01.2013 00:00:00 20.01.2013 23:59:59 99000";
	private static final String S3 = "6654   1 2 01.01.2013 00:00:00 31.01.2013 00:00:00 5000";
	private static final String S4 = "122856 1 1 20.01.2013 00:00:00 20.02.2013 23:59:59 11000";
	private static final String S5 = "122856 2 1 15.01.2013 00:00:00 25.01.2013 23:59:59 92000";
	private static final String S6 = "6654   1 2 12.01.2013 00:00:00 13.01.2013 00:00:00 4000";

	private static String[] ARGS = {S1,S2,S3,S4,S5,S6};
	/**
	 122856 1 1 01.01.2013 00:00:00 31.01.2013 23:59:59 11000
	 122856 2 1 10.01.2013 00:00:00 20.01.2013 23:59:59 99000
	 6654 1 2 01.01.2013 00:00:00 31.01.2013 00:00:00 5000

	 122856 1 1 20.01.2013 00:00:00 20.02.2013 23:59:59 11000
	 122856 2 1 15.01.2013 00:00:00 25.01.2013 23:59:59 92000
	 6654 1 2 12.01.2013 00:00:00 13.01.2013 00:00:00 4000
	 */

	private static SessionProvider sessionProvider = SessionProvider.instance();

	@org.junit.Test
	public void test() throws Throwable {
		main();
	}

	public static void main(String... args) throws Throwable {
		Service service = null;
		try {
			service = new Service();
			for (String arg : ARGS) {
				Data data = data(arg);
				service.persistDataList(Collections.singletonList(data));
			}
			//select all items
			Set<PriceTest> testResult = Stream.of(
					"122856 1 1 01.01.2013 00:00:00 20.02.2013 23:59:59 11000 ",
					"122856 2 1 10.01.2013 00:00:00 15.01.2013 00:00:00 99000",
					"122856 2 1 15.01.2013 00:00:00 25.01.2013 23:59:59 92000",
					"6654   1 2 01.01.2013 00:00:00 12.01.2013 00:00:00 5000",
					"6654   1 2 12.01.2013 00:00:00 13.01.2013 00:00:00 4000",
					"6654   1 2 13.01.2013 00:00:00 31.01.2013 00:00:00 5000"
			)
					.map(Test::data)
					.map(d -> new PriceTest(Price.create(d, Item.create(d))))
					.collect(Collectors.toSet());
			sessionProvider.doInTransaction(session -> {
				List<Price> list = session.createQuery("from Price where 1=1", Price.class).list();
				Set<PriceTest> persisted = list.stream().map(PriceTest::new).collect(Collectors.toSet());
				Assert.assertEquals(testResult, persisted);
			});
		} finally {
			if (service != null) service.dispose();
		}
	}

	private static Data data(String arg) {
		Scanner s = new Scanner(arg);
		Data data = new Data();
		try {
			data.setProductCode(s.next());
			data.setNumber(Integer.parseInt(s.next()));
			data.setDepart(Integer.parseInt(s.next()));
			data.setBegin(DateUtils.parseDate(s.next() + " " + s.next(), DATE_PATTERN));
			data.setEnd(DateUtils.parseDate(s.next() + " " + s.next(), DATE_PATTERN));
			data.setValue(Integer.parseInt(s.next()));
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
		s.close();
		return data;
	}


	static class ItemTest extends Item {

		ItemTest(Item item) {
			setId(item.getId());
			setProductCode(item.getProductCode());
			setDepart(item.getDepart());
		}

		@Override
		public boolean equals(Object o) {
			if (!o.getClass().equals(getClass())) return false;
			ItemTest et = (ItemTest) o;
			return getDepart().equals(et.getDepart())
					&& getProductCode().equals(et.getProductCode());
		}

		@Override
		public int hashCode() {
			return 1;
		}
	}

	static class PriceTest extends Price {

		PriceTest(Price p) {
			setItem(new ItemTest(p.getItem()));
			setNumber(p.getNumber());
			setBeginDate(p.getBeginDate());
			setEndDate(p.getEndDate());
			setValue(p.getValue());
		}

		@Override
		public boolean equals(Object o) {
			if (!o.getClass().equals(getClass())) return false;
			PriceTest p = (PriceTest) o;
			return p.getItem().equals(getItem())
					&& p.getNumber().equals(getNumber())
					&& p.getBeginDate().getTime()==getBeginDate().getTime()
					&& p.getEndDate().getTime()==getEndDate().getTime()
					&& p.getValue().equals(getValue());
		}

		@Override
		public int hashCode() {
			return 1;
		}
	}

}
