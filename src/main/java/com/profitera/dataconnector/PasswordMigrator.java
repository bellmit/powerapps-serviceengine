/*
 * Created by IntelliJ IDEA.
 * User: wso
 * Date: Apr 5, 2004
 * Time: 2:52:46 PM
 */
package com.profitera.dataconnector;

import com.profitera.descriptor.db.history.PasswordHistory;
import com.profitera.descriptor.db.user.User;
import com.profitera.persistence.SessionManager;
import com.profitera.util.*;
import java.io.*;
import java.util.Vector;
import oracle.toplink.queryframework.CursoredStream;
import oracle.toplink.queryframework.ReadAllQuery;
import oracle.toplink.sessions.UnitOfWork;

final class PasswordMigrator implements Runnable {
    private final String oldKey;
    private final String newKey;
    private final File passFile;

    private PasswordMigrator(String oldKey, String newKey, String passFile) {
        this.oldKey = readLineFromFile(oldKey);
        this.newKey = readLineFromFile(newKey);
        this.passFile = new File(passFile);
    }

    private static String readLineFromFile(String file) {
        final File keyFile = new File(file);
        BufferedReader r = null;
        try {
            final FileReader fi = new FileReader(keyFile);
            r = new BufferedReader(fi);
            return r.readLine();
        } catch (FileNotFoundException e) {
            throw ExceptionConverter.wrap(e);
        } catch (IOException e) {
            throw ExceptionConverter.wrap(e);
        } finally {
            try {
                if (null != r) r.close();
            } catch (IOException e) {}
        }
    }

    public final void run() {
        try {
            BufferedReader r = new BufferedReader(new FileReader(passFile));
            final String oldPass = r.readLine();
            r.close();
            final String newPass = migratePassword(oldPass);
            BufferedWriter w = new BufferedWriter(new FileWriter(passFile));
            w.write(newPass);
            w.close();
        } catch (Exception e) {
            throw ExceptionConverter.wrap(e);
        }
        final CursoredStream stream = TopLinkQuery.asCursoredStream(new ReadAllQuery(User.class),
            100, 100, SessionManager.getClientSession());
        final StreamIterator i = new StreamIterator(stream);
        new IteratorTransactionThread(i, 100) {
            protected void process(Object o, UnitOfWork uow) {
                final User user = (User) uow.registerExistingObject(o);
                user.setPassword(migratePassword(user.getPassword()));
                final Vector history = uow.registerAllObjects(user.getPasswordHistory());
                for (int j = 0; j < history.size(); j++) {
                    PasswordHistory h = (PasswordHistory) history.elementAt(j);
                    h.setPassword(migratePassword(h.getPassword()));
                }
            }
        }.run();
    }

    final String migratePassword(String oldPassword) {
        final String plaintext = PassUtils.desDecrypt(oldPassword, oldKey);
        return PassUtils.encrypt(plaintext.toCharArray(), newKey);
    }

    public static void main(String[] args) {
        switch (args.length) {
            default:
                new PasswordMigrator(args[0], args[1], args[2]).run();
                break;
            case 2:
            case 1:
            case 0:
                System.out.println("Usage: java PasswordMigrator <arg1> <arg2> <dbpass>\n" +
                    "Where: <arg1> is the original Key file\n"
                    + "       <arg2> is the new Key file\n"
                    + "       <dbpass> is the file containing the password to the database\n");
                return;
        }
    }
}