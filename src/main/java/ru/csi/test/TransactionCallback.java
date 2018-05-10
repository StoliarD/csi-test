package ru.csi.test;

import org.hibernate.Session;

public interface TransactionCallback {

    void action(Session session) throws Throwable;

}
