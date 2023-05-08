package com.bobocode.dao;

import com.bobocode.exception.DaoOperationException;
import com.bobocode.model.Product;
import com.bobocode.util.ExerciseNotCompletedException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.sql.DataSource;

public class ProductDaoImpl implements ProductDao {

    private static final String INSERT = "INSERT INTO products(name, producer, price, expiration_date) VALUES (?, ?, ?, ?);";
    private static final String UPDATE = "UPDATE products set name =?, producer= ?, price= ?, expiration_date=? WHERE id=?;";
    private final DataSource dataSource;

    public ProductDaoImpl(DataSource dataSource) {

        this.dataSource = dataSource;

    }

    @Override
    public void save(Product product) {
        try (Connection conn = dataSource.getConnection()) {


            PreparedStatement pstmt = conn.prepareStatement(INSERT, PreparedStatement.RETURN_GENERATED_KEYS);


            pstmt.setString(1, product.getName());
            pstmt.setString(2, product.getProducer());
            pstmt.setBigDecimal(3, product.getPrice());
            pstmt.setDate(4, Date.valueOf(product.getExpirationDate()));

            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                long key = rs.getLong(1);
                product.setId(key);
            }


        } catch (SQLException e) {
            throw new DaoOperationException("Error saving product: " + product.toString());

        }

    }

    @Override
    public List<Product> findAll() {

        try (Connection conn = dataSource.getConnection()) {

            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM products");
            List<Product> products = new ArrayList<>();
            while (rs.next()) {

                products.add(getObjectFromResultSet(rs));

            }
            return products;

        } catch (SQLException e) {
            throw new DaoOperationException("Error finding all products: ");

        }


    }

    public Product getObjectFromResultSet(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setId(rs.getLong("id"));
        product.setName(rs.getString("name"));

        product.setProducer(rs.getString("producer"));
        product.setPrice(rs.getBigDecimal("price"));
        product.setExpirationDate(rs.getDate("expiration_date").toLocalDate());
        product.setCreationTime(rs.getTimestamp("creation_time").toLocalDateTime());
        return product;
    }

    @Override
    public Product findOne(Long id) {
        try (Connection conn = dataSource.getConnection()) {

            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM products where id=" + id);

            if (rs.next()) {
                return getObjectFromResultSet(rs);

            } else {
                throw new DaoOperationException("Error when try to find one product: ");
            }


        } catch (SQLException e) {
            throw new DaoOperationException("Error when try to find one product: ");

        }
    }

    @Override
    public void update(Product product) {

        Objects.requireNonNull(product);
        if (product.getId() == null) {
            throw new DaoOperationException("Error update product: ");
        }
        try (Connection conn = dataSource.getConnection()) {

            PreparedStatement pstmt = conn.prepareStatement(UPDATE);
            pstmt.setString(1, product.getName());
            pstmt.setString(2, product.getProducer());
            pstmt.setBigDecimal(3, product.getPrice());
            pstmt.setDate(4, Date.valueOf(product.getExpirationDate()));
            pstmt.setLong(5, product.getId());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new DaoOperationException("Error update product: ");

        }


    }

    @Override
    public void remove(Product product) {

        if (product.getId() == null) {
            throw new DaoOperationException("Error delete");
        }

        try (Connection conn = dataSource.getConnection()) {

            Statement statement = conn.createStatement();
            statement.execute("DELETE FROM products WHERE id=" + product.getId());

        } catch (SQLException e) {
            throw new DaoOperationException("Error delete");
        }

    }

}
