package restserver.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class Postgresql extends DbAccessor {
    private final static String DRIVER_BDD = "org.postgresql.Driver";
    private final static String URL = "jdbc:postgresql://192.168.2.19:5432/postgres";
    private final String RQT_DROP = "DROP TABLE personnes;";
    private final String RQT_CREATE_TBL = "CREATE TABLE personnes ( id SERIAL, nom VARCHAR(32), prenom VARCHAR(32));";
    private final String RQT_INSERT = "INSERT INTO personnes (nom,prenom) VALUES('NOM', 'PRENOM');";
    private final String RQT_SELECT = "SELECT * FROM personnes;";


    public Postgresql(String driver, String uri, String login, String pwd) throws ClassNotFoundException {
        super(driver, uri, login, pwd);
    }

    public static void main(String[] args) throws ClassNotFoundException {
        Class.forName(DRIVER_BDD);
        Postgresql testConnexion = new Postgresql(DRIVER_BDD, URL, "postgres", null);

        try {
            Connection c = testConnexion.connect();
            testConnexion.deleteTableDeTest(c);
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

    private void deleteTableDeTest(Connection c) throws SQLException {
        final PreparedStatement ps = c.prepareStatement(RQT_DROP);
        ps.execute();

    }

    public void disconnect(Connection c) throws SQLException {
        super.disconnect(c);
    }

}
