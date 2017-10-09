package restserver.db;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class DbAccessor {
    private String driver;
    private String uri;
    private String login;
    private String pwd;

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

    public int writeAndGetGeneratedKey(Connection c, String req, Map<String, Object> params, Map<String, String> knownTypes) throws SQLException {
        // TODO
        PreparedStatement ps;
        if (req.startsWith("insert") || req.startsWith("update")) {
            ps = c.prepareStatement(req, PreparedStatement.RETURN_GENERATED_KEYS);
        } else {
            ps = c.prepareStatement(req);
        }

        int i = 1;
        for (Map.Entry<String, Object> keyValueParameter : params.entrySet()) {
            final String expectedType = knownTypes.get(keyValueParameter.getKey());
            if ("Integer".equals(expectedType)) {
                ps.setInt(i++, Integer.parseInt(keyValueParameter.getValue().toString()));
            } else if ("String".equals(expectedType)) {
                ps.setString(i++, keyValueParameter.getValue().toString());
            } else {
                ps.setObject(i++, keyValueParameter.getValue());
            }

        }
        ps.executeUpdate();
        try {
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                return -1;
            }
        } catch (SQLException e) {
            // TODO log
            e.printStackTrace();
            return -2;
        } finally {
            ps.close();
        }
    }

    public int writeAndGetNumberUpdated(Connection c, String req, Map<String, Object> params, Map<String, String> knownTypes) throws SQLException {
        // TODO same here ?
        PreparedStatement ps;
        if (req.startsWith("insert") || req.startsWith("update")) {
            ps = c.prepareStatement(req);
        } else {
            ps = c.prepareStatement(req);
        }

        int i = 1;
        for (Map.Entry<String, Object> keyValueParameter : params.entrySet()) {
            final String expectedType = knownTypes.get(keyValueParameter.getKey());
            if ("Integer".equals(expectedType)) {
                ps.setInt(i++, Integer.parseInt(keyValueParameter.getValue().toString()));
            } else if ("String".equals(expectedType)) {
                ps.setString(i++, keyValueParameter.getValue().toString());
            } else {
                ps.setObject(i++, keyValueParameter.getValue());
            }
        }
        return ps.executeUpdate();
    }

    public JsonArray read(Connection c, Request request, LinkedHashMap<String, Object> params) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(request.getRequest())) {
            int idx = 1;
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                Class aClass = request.getExpectedParameters().get(entry.getKey());
                if (Integer.class.equals(aClass)) {
                    ps.setInt(idx++, Integer.parseInt(entry.getValue().toString()));
                } else {
                    ps.setObject(idx++, entry.getValue());
                }
            }
            ps.execute();
            try (ResultSet rs = ps.getResultSet()) {
                final JsonArray result = new JsonArray();
                while (rs.next()) {
                    JsonObject row = new JsonObject();
                    for (Map.Entry<String, String> entry : request.getExpectedResultPerRow().entrySet()) {
                        if (String.class.getSimpleName().equals(entry.getValue())) {
                            String string = rs.getString(entry.getKey());
                            row.put(entry.getKey(), string);
                        } else if (Long.class.getSimpleName().equals(entry.getValue())) {
                            long aLong = rs.getLong(entry.getKey());
                            row.put(entry.getKey(), aLong);
                        } else if (Integer.class.getSimpleName().equals(entry.getValue())) {
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
