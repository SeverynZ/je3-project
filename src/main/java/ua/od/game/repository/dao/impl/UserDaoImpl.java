package ua.od.game.repository.dao.impl;

import ua.od.game.model.UserEntity;
import ua.od.game.repository.dao.UserDao;
import ua.od.game.repository.helper.SqlHelper;
import java.sql.ResultSet;

public class UserDaoImpl implements UserDao {

    @Override
    public String createNewUser(UserEntity user){
        String token = null;
        int countUser = 0;

        countUser = SqlHelper.prepareStatement("SELECT count(*) from User where name = ?", pstmt -> {
            pstmt.setString(1, user.getName());
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getInt("count(*)") : 0;
        });
        if(countUser > 0) {
            try {
                throw new Error("This user already exists!!!!");
            } catch (Error e) {
                System.out.println(e.toString());
                return "";
            }
        }
        token = user.getToken();
        SqlHelper.prepareStatement("INSERT INTO User(name, password, token) values(?,?,?)", pstmt -> {
            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getToken());
            return pstmt.executeUpdate();
        });
        SqlHelper.prepareStatement("INSERT INTO Account(user_id) select id from User where name = ?", pstmt -> {
            pstmt.setString(1, user.getName());
            return pstmt.executeUpdate();
        });
        return token;
    }

    @Override
    public String loginUser(UserEntity user) {
        String token = null;

        token = SqlHelper.prepareStatement("UPDATE User Set token = ? where name = ? and password = ?",
                pstmt -> {
            pstmt.setString(1, user.getToken());
            pstmt.setString(2, user.getName());
            pstmt.setString(3, user.getPassword());
            return pstmt.executeUpdate() > 0 ? user.getToken() : "";
        });
        if(token.isEmpty()) {
            try {
                throw new Error("This user does not exists!!!!");
            } catch (Error e) {
                System.out.println(e.toString());
                return "";
            }
        }
        return token;
    }

    @Override
    public boolean logoutUser(String token) {
        boolean logout;

        logout = SqlHelper.prepareStatement("UPDATE User Set token = '' where token = ?", pstmt -> {
            pstmt.setString(1, token);
            return pstmt.executeUpdate() > 0;
        });
        if(!logout) {
            try {
                throw new Error("This token is wrong!!!!");
            } catch (Error e) {
                System.out.println(e.toString());
            }
        }
        return logout;
    }

    @Override
    public UserEntity getUserByToken(String token) {
       UserEntity user = null;

       user = SqlHelper.prepareStatement("SELECT * from User where token = ?", pstmt -> {
            pstmt.setString(1, token);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? new UserEntity(){{
                setId(rs.getInt("id"));
                setName(rs.getString("name"));
                setPassword(rs.getString("password"));
                setToken(rs.getString("token"));
            }} : null;
            });
        if(user == null) {
            try {
                throw new Error("Wrong token!!!!!");
            } catch (Error e) {
                System.out.println(e.toString());
            }
        }
       return user;
  }
}

class Error extends Exception {

    public Error(String s) {
        super(s);
    }
}
