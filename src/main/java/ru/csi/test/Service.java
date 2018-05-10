package ru.csi.test;

import org.hibernate.Session;
import org.hibernate.query.Query;
import ru.csi.test.entities.Data;
import ru.csi.test.hbn_entities.Item;
import ru.csi.test.hbn_entities.Price;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class Service {

	private static SessionProvider sessionProvider = SessionProvider.instance();

	private final static String PRICE_QUERY =
			"from Price p where p.item=:item and p.number=:number and (p.beginDate<=:end or p.endDate>=:begin)";
	private final static String ITEM = "item";
	private final static String NUMBER = "number";
	private final static String BEGIN = "begin";
	private final static String END = "end";

	private final static String ITEM_QUERY = "from Item i where i.productCode=:code and i.depart=:depart";
	private final static String CODE = "code";
	private final static String DEPART = "depart";

	public void persistDataList(List<Data> dataList) throws Throwable {
		sessionProvider.doInTransaction(
				session -> {
					for (Data data : dataList) {
						persistNewData(data, session);
					}
				}

		);
	}

	private void persistNewData(Data data, Session session) throws Throwable {
		Integer itemId = findOrCreateItem(data.getProductCode(), data.getDepart(), session);
		Price price = Price.create(data, new Item(itemId));

		Query<Price> query = session.createQuery(PRICE_QUERY, Price.class);
		query.setParameter(ITEM, new Item(itemId));
		query.setParameter(NUMBER, data.getNumber());
		query.setParameter(BEGIN, data.getBegin());
		query.setParameter(END, data.getEnd());

		List<Price> queried = query.list(); //assert size <=2
		queried.sort(Comparator.comparing(Price::getBeginDate));

		List<Price> prices = new ArrayList<>();
		prices.add(price);
		for (Price persisted : queried) {
			List<Price> newPrices = new ArrayList<>();
			for (Price p : prices) {
				List<Price> container = new ArrayList<>();
				container.add(p);
				boolean b = mergeWithPersisted(container, persisted);
				if (b) session.delete(persisted);
				newPrices.addAll(container);
			}
			prices = newPrices;
		}
		prices.forEach(session::persist);
	}

	/**
	 * Price.value doesn't mutate method
	 * @param prices singleton-list with db candidate, will be mutated
	 * @return persisted should be deleted or not
	 */
	private boolean mergeWithPersisted(List<Price> prices, Price persisted) {
		Price p = prices.get(0);
		Date begin = p.getBeginDate();
		Date persistedBegin = persisted.getBeginDate();
		Date end = p.getEndDate();
		Date persistedEnd = persisted.getEndDate();
		if (end.before(persistedBegin) || persistedEnd.before(begin)) { // dates dont intersect
			return false;
		}
		if (p.getValue().equals(persisted.getValue())) {
			p.setBeginDate(begin.before(persistedBegin) ? begin : persistedBegin); // min
			p.setEndDate(end.after(persistedEnd) ? end : persistedEnd); // max
			return true;
		}

		if (begin.before(persistedBegin)) {
			persisted.setBeginDate(end);
		}
		if (end.after(persistedEnd)) {
			persisted.setEndDate(begin);
		}
		//persisted didn't change means that persisted contains p
		if (persisted.getBeginDate().equals(persistedBegin) && persisted.getEndDate().equals(persistedEnd)) {
			Price copy = persisted.copy();
			persisted.setEndDate(begin);
			copy.setBeginDate(end);
			if (!persisted.getEndDate().after(persisted.getBeginDate())) {//we don't need to persist copy
				persisted.setBeginDate(copy.getBeginDate());
				persisted.setEndDate(copy.getEndDate());
			} else if (copy.getEndDate().after(copy.getBeginDate())) {
				prices.add(copy);
			}
		}
		return !persisted.getEndDate().after(persisted.getBeginDate());
	}


	/**
	 * @return persisted item ID
	 */
	private Integer findOrCreateItem(String code, int depart, Session session) {
		Item item = findItem(code, depart, session);
		if (item == null) {
			return (Integer) session.save(new Item(code, depart));
		}
		return item.getId();
	}

	/**
	 * this should be in DAO
	 *
	 * @return persisted obj or null
	 */
	private Item findItem(String code, int depart, Session session) {
		Query<Item> query = session.createQuery(ITEM_QUERY, Item.class);
		query.setParameter(CODE, code);
		query.setParameter(DEPART, depart);
		List<Item> list = query.list(); // assert size <= 1
		return list.isEmpty() ? null : list.get(0);
	}

	public void dispose() {
		sessionProvider.close();
	}



}
