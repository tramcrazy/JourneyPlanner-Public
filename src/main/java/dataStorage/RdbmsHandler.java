package dataStorage;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class RdbmsHandler {

    private final String databasePath;
    private final Connection connection;
    private final Statement statement;

    public RdbmsHandler(String databasePath) {
        this.databasePath = databasePath;
        connection = connectToDatabase();
        statement = createStatement();
    }

    private Connection connectToDatabase() {
        Connection connection = null;

        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
        } catch (SQLException e) {
            System.out.printf("An error occurred when trying to connect to the SQLite database at %s.%n", databasePath);
            e.printStackTrace();
        }

        return connection;
    }

    private Statement createStatement() {
        Statement statement = null;

        try {
            statement = connection.createStatement();
        } catch (SQLException e) {
            System.out.println("An error occurred when trying to create a statement for the SQLite database connection.");
            e.printStackTrace();
        }

        return statement;
    }

    public void createTable(String tableName, String[] fields, String primaryKey, String[] foreignKeys, String[] referencedTables, String[] referencedKeys) {
        // Builds and runs statements of the format 'CREATE TABLE tableName (field1, field2, ... [, PRIMARY KEY (field1)][, FOREIGN KEY (fKey) REFERENCES table2Name (table2Key)];'

        if (!Objects.equals(primaryKey, "")) {
            boolean validPrimaryKey = false;

            for (String field : fields) {
                if (field.contains(primaryKey)) {
                    validPrimaryKey = true;
                    break;
                }
            }

            if (!validPrimaryKey) {
                throw new RuntimeException("Primary key must be one of the defined fields. Key " + primaryKey + " is not in the given list of fields.");
            }
        }

        StringBuilder fieldsString = new StringBuilder("(");

        for (int i = 0; i < fields.length; i++) {
            if (i != fields.length - 1) {
                fieldsString.append(fields[i]).append(", ");
            } else {
                fieldsString.append(fields[i]);
            }
        }

        if (!Objects.equals(primaryKey, "")) {
            fieldsString.append(", PRIMARY KEY (").append(primaryKey).append(")");
        }

        for (int i = 0; i < foreignKeys.length; i++) {
            if (!Objects.equals(foreignKeys[i], "") && !Objects.equals(referencedTables[i], "") && !Objects.equals(referencedKeys[i], "")) {
                fieldsString.append(", FOREIGN KEY (").append(foreignKeys[i]).append(") REFERENCES ").append(referencedTables[i]).append("(").append(referencedKeys[i]).append(")");
            }
        }

        fieldsString.append(")");

        String finalString = "CREATE TABLE " + tableName + " " + fieldsString + ";";

        try {
            statement.executeUpdate(finalString);
        } catch (SQLException e) {
            System.out.printf("An error occurred when trying to create the %s table.%n", tableName);
            e.printStackTrace();
        }
    }

    public void fillTable(String tableName, String[][] values) {
        /*
        Note - the values should be given in this format:
        Numbers - no quotes within the string. e.g. "49"
        Strings (VARCHAR etc.) - single quotes within the string e.g. "'Jonathan'" - NOTE that you need to make sure that any ' within the string are escaped by doubling them
        Make sure to match these with the field types you defined when creating the table
        Feed this method a 2D array with each row being a row to insert into the table
         */

        String rowString;
        String sqlString;

        for (String[] row : values) {
            rowString = String.join(", ", row);
            sqlString = "INSERT INTO %s VALUES (%s);".formatted(tableName, rowString);

            try {
                statement.executeUpdate(sqlString);
            } catch (SQLException e) {
                System.out.printf("An error occurred when trying to insert values into the %s table.%n", tableName);
                e.printStackTrace();
            }
        }
    }

    public boolean checkTableDoesNotExist(String tableName) {
        String sqlString = "SELECT name FROM sqlite_master WHERE type='table' AND name='%s';".formatted(tableName);

        try {
            ResultSet resultSet = statement.executeQuery(sqlString);
            resultSet.next();
            return resultSet.getRow() == 0;
        } catch (SQLException e) {
            System.out.printf("An error occurred when trying to check the existence of the table %s.%n", tableName);
            e.printStackTrace();
        }

        return true;
    }

    public boolean checkRowDoesNotExist(String tableName, String[] fieldNames, String[] equalTo) {
        ResultSet resultSet = selectAllWhere(tableName, fieldNames, equalTo);

        try {
            resultSet.next();
            return resultSet.getRow() == 0;
        } catch (SQLException e) {
            System.out.printf("An error occurred when trying to check for the existence of a row with field names %s matching values %s in table %s.%n", Arrays.toString(fieldNames), Arrays.toString(equalTo), tableName);
        }

        return true;
    }

    private ResultSet selectAllWhere(String tableName, String[] fieldNames, String[] equalTo) {

        StringBuilder conditionsBuilder = new StringBuilder();

        for (int i = 0; i < fieldNames.length; i++) {
            if (i == 0) {
                conditionsBuilder.append("SELECT * FROM ").append(tableName).append(" WHERE ").append(fieldNames[0]).append("='").append(equalTo[0]).append("'");
            } else {
                conditionsBuilder.append(" AND ").append(fieldNames[i]).append("='").append(equalTo[i]).append("'");
            }
        }

        conditionsBuilder.append(";");

        try {
            return statement.executeQuery(conditionsBuilder.toString());
        } catch (SQLException e) {
            System.out.printf("An error occurred when trying to select data with field names %s matching values %s from table %s.%n", Arrays.toString(fieldNames), Arrays.toString(equalTo), tableName);
            e.printStackTrace();
        }

        return null;
    }

    public String[][] selectStringFieldsWhereLike(String tableName, String searchField, String searchTerm, String[] selectedFields) {
        ArrayList<String[]> resultList = new ArrayList<>();
        String selectedFieldsString = String.join(", ", selectedFields);
        String sqlString = "SELECT %s FROM %s WHERE %s LIKE '%%%s%%';".formatted(selectedFieldsString, tableName, searchField, searchTerm);

        String[] workingArray;
        try {
            ResultSet resultSet = statement.executeQuery(sqlString);
            while (resultSet.next()) {
                workingArray = new String[selectedFields.length];
                for (int i = 0; i < selectedFields.length; i++) {
                    workingArray[i] = resultSet.getString(i + 1);
                }
                resultList.add(workingArray);
            }
        } catch (SQLException e) {
            System.out.printf("An error occurred while trying to search for data in field %s.%s matching search term %s.".formatted(tableName, searchField, searchTerm));
        }

        return resultList.toArray(new String[0][0]);
    }

    public String[] selectStringFieldWhere(String tableName, String whereName, String equalTo, String selectName) {
        String sqlString = "SELECT %s FROM %s WHERE %s=%s;".formatted(selectName, tableName, whereName, equalTo);
        ArrayList<String> resultsList = new ArrayList<>();

        try {
            ResultSet resultSet = statement.executeQuery(sqlString);
            while (resultSet.next()) {
                resultsList.add(resultSet.getString(1));
            }
        } catch (SQLException e) {
            System.out.printf("An error occurred when trying to select data with %s=%s from table %s.%n", whereName, equalTo, tableName);
            e.printStackTrace();
        }

        return resultsList.toArray(new String[0]);
    }

    public String[] selectDistinctStringFieldWithJoin(String tableName, String joinedTableName, String joinName, String joinEqualTo, String[] whereNames, String[] equalTo, String selectName) {
        StringBuilder sqlBuilder = new StringBuilder("SELECT DISTINCT ");
        ArrayList<String> resultsList = new ArrayList<>();

        sqlBuilder.append(selectName);
        sqlBuilder.append(" FROM ").append(tableName);
        sqlBuilder.append(" INNER JOIN ").append(joinedTableName);
        sqlBuilder.append(" ON ").append(joinName);
        sqlBuilder.append(" = ").append(joinEqualTo);
        sqlBuilder.append(" WHERE ");

        for (int i = 0; i < whereNames.length; i++) {
            if (i == 0) {
                sqlBuilder.append(whereNames[0]).append("=").append(equalTo[0]);
            } else {
                sqlBuilder.append(" AND ").append(whereNames[i]).append("=").append(equalTo[i]);
            }
        }

        sqlBuilder.append(";");

        try {
            ResultSet resultSet = statement.executeQuery(sqlBuilder.toString());

            while (resultSet.next()) {
                resultsList.add(resultSet.getString(1));
            }
        } catch (SQLException e) {
            System.out.printf("An error occurred while trying to select distinct fields from the joined tables " + tableName + " and " + joinedTableName + ".");
        }

        return resultsList.toArray(new String[0]);


    }

    public String[][] selectStringFieldsWhere(String tableName, String whereName, String equalTo, String[] selectedFields) {
        ArrayList<String[]> resultList = new ArrayList<>();
        String selectedFieldsString = String.join(", ", selectedFields);
        String sqlString = "SELECT %s FROM %s WHERE %s=%s;".formatted(selectedFieldsString, tableName, whereName, equalTo);

        String[] workingArray;
        try {
            ResultSet resultSet = statement.executeQuery(sqlString);
            while (resultSet.next()) {
                workingArray = new String[selectedFields.length];
                for (int i = 0; i < selectedFields.length; i++) {
                    workingArray[i] = resultSet.getString(i + 1);
                }
                resultList.add(workingArray);
            }
        } catch (SQLException e) {
            System.out.printf("An error occurred while trying to select data in table %s matching %s on field %s".formatted(tableName, equalTo, whereName));
        }

        return resultList.toArray(new String[0][0]);
    }

    public String[] selectStringField(String tableName, String fieldName) {
        String sqlString = "SELECT %s FROM %s;".formatted(fieldName, tableName);
        ResultSet resultSet;
        ArrayList<String> results = new ArrayList<>();

        try {
            resultSet = statement.executeQuery(sqlString);
            while (resultSet.next()) {
                results.add(resultSet.getString(1));
            }
        } catch (SQLException e) {
            System.out.printf("An error occurred when trying to select data from the %s column from table %s.%n", fieldName, tableName);
            e.printStackTrace();
        }

        return results.toArray(new String[0]);
    }

    public String[][] selectStringFields(String tableName, String[] fieldNames) {
        ArrayList<String[]> resultList = new ArrayList<>();
        String fieldNamesString = String.join(", ", fieldNames);

        String sqlString = "SELECT %s FROM %s;".formatted(fieldNamesString, tableName);

        String[] workingArray;
        try {
            ResultSet resultSet = statement.executeQuery(sqlString);
            while (resultSet.next()) {
                workingArray = new String[fieldNames.length];
                for (int i = 0; i < fieldNames.length; i++) {
                    workingArray[i] = resultSet.getString(i + 1);
                }
                resultList.add(workingArray);
            }
        } catch (SQLException e) {
            System.out.printf("An error occurred while trying to select data in table %s".formatted(tableName));
        }

        return resultList.toArray(new String[0][0]);
    }

    public void deleteAllRows(String tableName) {
        String sqlString = "DELETE FROM %s;".formatted(tableName);

        try {
            statement.executeUpdate(sqlString);
        } catch (SQLException e) {
            System.out.printf("An error occurred when trying to delete all rows of table %s.%n", tableName);
            e.printStackTrace();
        }
    }

    public void closeDbConnection() {
        try {
            statement.close();
            connection.close();
        } catch (SQLException e) {
            System.out.println("An error occurred when trying to close the statement and connection for the database file " + databasePath + ".");
            e.printStackTrace();
        }
    }
}
