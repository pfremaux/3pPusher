package thirdpartypusher.db;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DbAccessor {
    private String driver;
    private String uri;
    private String login;
    private String pwd;

    public DbAccessor() {

    }

    public DbAccessor(String driver, String uri, String login, String pwd) {
        this.driver = driver;
        this.uri = uri;
        this.login = login;
        this.pwd = pwd;
        try {
            Class.forName(getDriver());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Connection connect() throws SQLException {
        Connection bdd;
        bdd = DriverManager.getConnection(getUri(),
                getLogin(),
                getPwd());
        return bdd;
    }

    public int execute(Connection c, String req, List params) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(req)) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            return ps.executeUpdate();
        }
    }

    public List<Map<String, Object>> read(Connection c, Request request, List params) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(request.getRequest())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            ps.execute();
            try (ResultSet rs = ps.getResultSet()) {
                final List<Map<String, Object>> result = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (Map.Entry<String, Class> entry : request.getExpectedResultPerRow().entrySet()) {
                        if (String.class.getName().equals(entry.getValue().getName())) {
                            String string = rs.getString(entry.getKey());
                            row.put(entry.getKey(), string);
                        } else if (Long.class.getName().equals(entry.getValue().getName())) {
                            long aLong = rs.getLong(entry.getKey());
                            row.put(entry.getKey(), aLong);
                        } else if (Integer.class.getName().equals(entry.getValue().getName())) {
                            int anInt = rs.getInt(entry.getKey());
                            row.put(entry.getKey(), anInt);
                        } else {
                            Object object = rs.getObject(entry.getKey());
                            row.put(entry.getKey(), object);
                        }
                    }
                    result.add(row);
                }
                return result;
            }
        }
    }



    public void disconnect(Connection c) throws SQLException {
        c.close();
    }

    @Override
    public String toString() {
        return "DbAccessor{" +
                "driver='" + driver + '\'' +
                ", uri='" + uri + '\'' +
                ", login='" + login + '\'' +
                ", pwd='" + pwd + '\'' +
                '}';
    }

    public String getDriver() {
        return driver;
    }

    public String getUri() {
        return uri;
    }

    public String getLogin() {
        return login;
    }

    public String getPwd() {
        return pwd;
    }


    public void setDriver(String driver) {
        this.driver = driver;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }
}
