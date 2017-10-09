package restserver.db;

import java.sql.*;

public final class Hsqldb extends DbAccessor {
    private final static String DRIVER_BDD = "org.hsqldb.jdbcDriver";
    private final static String URL = "jdbc:hsqldb:mem:testHSQLDB";
    private final String RQT_CREATE_TBL = "CREATE TABLE personnes ( id INTEGER IDENTITY, nom VARCHAR(32), prenom VARCHAR(32))";
    private final String RQT_INSERT = "INSERT INTO personnes (nom,prenom) VALUES('NOM', 'PRENOM')";
    private final String RQT_SELECT = "SELECT * FROM personnes";


    public Hsqldb(String driver, String uri, String login, String pwd) throws ClassNotFoundException {
        super(driver, uri, login, pwd);
    }

    public static void main(String[] args) throws ClassNotFoundException {
        Hsqldb testConnexion = new Hsqldb(DRIVER_BDD, URL, null, null);
        try {
            Connection c = testConnexion.connect();
            testConnexion.creerTableDeTest(c);
            testConnexion.insererDonneeTest(c);
            testConnexion.lireDonneeTest(c);
            testConnexion.disconnect(c);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void lireDonneeTest(Connection c) throws SQLException {
        final PreparedStatement ps = c.prepareStatement(RQT_SELECT);
        ps.execute();
        ResultSet resultSet = ps.getResultSet();
        while (resultSet.next()) {
            String nom = resultSet.getString("nom");
            String prenom = resultSet.getString("prenom");
            System.out.println(nom + " " + prenom);
        }
    }

    private void insererDonneeTest(Connection c) throws SQLException {
        final PreparedStatement ps = c.prepareStatement(RQT_INSERT);
        ps.executeUpdate();
    }

    private void creerTableDeTest(Connection c) throws SQLException {
        final PreparedStatement ps = c.prepareStatement(RQT_CREATE_TBL);
        ps.execute();

    }

    public void disconnect(Connection c) throws SQLException {
        try (Statement st = c.createStatement()) {
            // Nécessaire pour clôturer proprement
            st.execute("SHUTDOWN");
        }
        super.disconnect(c);
    }

}
