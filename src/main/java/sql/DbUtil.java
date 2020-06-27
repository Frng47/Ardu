package sql;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * url="jdbc:mysql://localhost:3307/arduinoDB?serverTimezone=UTC";
 * user="root";
 * password="root";
 */

public class DbUtil {
    private String url;
    private String user;
    private String password;
    private String table = "photores";
    Long time=667l;
    Double value=1.2;



    public DbUtil(String url,String user,String password){
        this.url=url;
        this.user=user;
        this.password=password;
    }
    public void writePrSt(){

    }
    public void write(Map<Long,Double> buff)  {
            try{
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            Connection con = DriverManager.getConnection(url, user, password);
            con.setAutoCommit(false);
            StringBuilder query=new StringBuilder();
            query.append("insert into "+table+" values (?,?);");
            PreparedStatement preparedStatement=con.prepareStatement(query.toString());
            try{
            for(Map.Entry<Long,Double> entry:buff.entrySet()){
                preparedStatement.setLong(1,entry.getKey());
                preparedStatement.setDouble(2,entry.getValue());
                preparedStatement.execute();
            }
            con.commit();}finally {
                preparedStatement.close();
                con.close();
            }
            } catch (SQLException e) {
                e.printStackTrace();
            }catch (ClassNotFoundException e){
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
    }
    public Map<Long,Double> read(){
        Map<Long,Double> map=new HashMap<>();
        try{
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            Connection con = DriverManager.getConnection(url, user, password);
            con.setAutoCommit(false);
            ResultSet rs;
            StringBuilder query=new StringBuilder();
            query.append("select" +
                            " time, value " +
                        "from " +
                        table+
                        " where "+
                    "time BETWEEN ? AND ? "+
                    "order by time");
            PreparedStatement preparedStatement=con.prepareStatement(query.toString());
            try{
                    preparedStatement.setLong(1,0);
                    preparedStatement.setLong(2,1693015312215l);
                    rs=preparedStatement.executeQuery();
                    con.commit();
                    while (rs.next()){
                    map.put(rs.getLong(1),rs.getDouble(2));
                    }
            }finally {
                preparedStatement.close();
                con.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }catch (ClassNotFoundException e){
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

        return map;
    }
   public static void main(String[] args){
       Random random=new Random();
       Map<Long,Double> map=new HashMap();
/*
       for (int i = 0; i <10 ; i++) {
           map.put(new Long(i),random.nextDouble());
       }
*/

       //new DbUtil("jdbc:mysql://localhost:3307/arduinoDB?serverTimezone=UTC","root","root").write(map);
       System.out.println(new DbUtil("jdbc:mysql://localhost:3307/arduinoDB?serverTimezone=UTC","root","root").read());
   }
}



