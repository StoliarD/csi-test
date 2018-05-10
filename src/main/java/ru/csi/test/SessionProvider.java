package ru.csi.test;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import ru.csi.test.hbn_entities.Item;
import ru.csi.test.hbn_entities.Price;

public class SessionProvider {

    private static final SessionProvider INSTANCE = new SessionProvider();

    private final SessionFactory sessionFactory;

    private SessionProvider() {
        sessionFactory = buildSessionFactory();
    }

    public Session openSession() {
        return sessionFactory.openSession();
    }

    public void doInTransaction(TransactionCallback callback) throws Throwable {
        Session session = null;
        Transaction transaction = null;
        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();
            callback.action(session);
            transaction.commit();
        } catch (Throwable e) {
            try {
                if (transaction!= null) transaction.rollback();
            } catch (Exception e2) {
                System.out.println("error while tx rollback" + e2);
            }
            throw e;
        } finally {
            if (session != null) session.close();
        }
    }

    private static SessionFactory buildSessionFactory() {
        try {
            StandardServiceRegistry standardRegistry =
                    new StandardServiceRegistryBuilder().configure("hibernate.cfg.xml").build();
            Metadata metadata = new MetadataSources(standardRegistry)
                    .addAnnotatedClass(Item.class)
                    .addAnnotatedClass(Price.class)
                    .getMetadataBuilder().build();
            return metadata.getSessionFactoryBuilder().build();
        }
        catch (Throwable ex) {
            // Make sure you log the exception, as it might be swallowed
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    void close() {
        sessionFactory.close();
    }

    public static SessionProvider instance() {
        return INSTANCE;
    }

}
