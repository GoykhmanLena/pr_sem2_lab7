package ru.lenok.server.daos;

import ru.lenok.common.FullOffer;
import ru.lenok.common.Offer;
import ru.lenok.common.OfferStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OfferDAO {
    private Connection connection;
    private static final String CREATE_OFFER = """
                INSERT INTO offer (
                    labWork_id,
                    product_id,
                    status
                ) VALUES (?, ?, ?)
                RETURNING id
            """;

    private static final String UPDATE_OFFER = """
                UPDATE offer
                SET
                    labWork_id = ?,
                    product_id = ?,
                    status = ?
                WHERE id = ?        
            """;

    private static final String SELECT_OFFERS_BY_LAB_WORK_OWNER = """
            SELECT o.id, lw.id, lw.name, p.id, p.name, u_pr.id, u_pr.name, o.status 
            FROM offer o
            JOIN lab_work lw ON o.labWork_id=lw.id
            JOIN product p ON o.product_id=p.id
            JOIN users u_lw ON u_lw.id=lw.owner_id
            JOIN users u_pr ON u_pr.id=p.owner_id
            WHERE u_lw.id = ? AND o.status LIKE 'OPEN'
            """;

    private static final String SELECT_OFFERS_BY_PRODUCT_OWNER = """
            SELECT o.id, lw.id, lw.name, p.id, p.name, u_lw.id, u_lw.name, o.status 
            FROM offer o
            JOIN lab_work lw ON o.labWork_id=lw.id
            JOIN product p ON o.product_id=p.id
            JOIN users u_lw ON u_lw.id=lw.owner_id
            JOIN users u_pr ON u_pr.id=p.owner_id
            WHERE u_pr.id = ? AND o.status LIKE 'OPEN'
            """;
    private static final String SELECT_OFFER_BY_ID = """
            SELECT o.id, lw.id, lw.name, p.id, p.name, u_lw.id, u_lw.name, o.status 
            FROM offer o
            JOIN lab_work lw ON o.labWork_id=lw.id
            JOIN product p ON o.product_id=p.id
            JOIN users u_lw ON u_lw.id=lw.owner_id
            JOIN users u_pr ON u_pr.id=p.owner_id
            WHERE o.id = ?
            """;

    private static final String SELECT_OFFERS_BY_PRODUCT_ID = """
            SELECT * FROM offer
            WHERE product_id = ?
            """;

    private static final String SELECT_OFFERS_BY_LAB_WORK_ID = """
            SELECT * FROM offer
            WHERE labWork_id = ?
            """;
    public OfferDAO(DBConnector dbConnector, boolean dbReinit) throws SQLException {
        connection = dbConnector.getConnection();
        init(dbReinit);
    }

    private void init(boolean dbReinit) throws SQLException {
        initScheme(dbReinit);
    }
    private void initScheme(boolean reinitDB) throws SQLException {

        String dropALL =
                        "DROP TABLE IF EXISTS offer;\n" +
                        "DROP SEQUENCE IF EXISTS offer_seq;";

        String createSequence = "CREATE SEQUENCE IF NOT EXISTS offer_seq START 1;";

        String createTable = "CREATE TABLE IF NOT EXISTS offer (\n" +
                "    id BIGINT DEFAULT nextval('offer_seq') PRIMARY KEY,\n" +
                "    labWork_id BIGINT NOT NULL,\n" +
                "    product_id BIGINT NOT NULL,\n" +
                "    status VARCHAR(256) NOT NULL,\n" +
                "    CONSTRAINT fk_labwork FOREIGN KEY (labWork_id) REFERENCES lab_work(id),\n" +
                "    CONSTRAINT fk_product FOREIGN KEY (product_id) REFERENCES product(id)\n" +
                ");";



        try (Statement stmt = connection.createStatement()) {
            if (reinitDB) {
                stmt.executeUpdate(dropALL);
            }
            stmt.executeUpdate(createSequence);
            stmt.executeUpdate(createTable);
            connection.commit();
        } catch (SQLException e){
            connection.rollback();
            throw e;
        }
    }

    public Offer insert(Offer offer) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(CREATE_OFFER)) {

            pstmt.setLong(1, offer.getLabWorkId());
            pstmt.setLong(2, offer.getProductId());
            pstmt.setString(3, offer.getStatus().name());

            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
                    long offerId = resultSet.getLong(1);
                    Offer offerFromDB = new Offer(offer.getLabWorkId(), offer.getProductId(), offer.getStatus(), offerId);
                    return offerFromDB;
                } else {
                    throw new SQLException("Ошибка при вставке предложения, " + offer);
                }
            }
        }
    }

    public List<FullOffer> selectOffersByLabWorkOwner(Long userId) throws SQLException {
        List<FullOffer> userOffers = new ArrayList<>();

        try (PreparedStatement pstmt = connection.prepareStatement(SELECT_OFFERS_BY_LAB_WORK_OWNER)){

            pstmt.setLong(1, userId);

            try(ResultSet resultSet = pstmt.executeQuery()){
                while (resultSet.next()){
                    long offerId = resultSet.getLong(1);
                    long labWorkId = resultSet.getLong(2);
                    String labWorkName = resultSet.getString(3);
                    long productId = resultSet.getLong(4);
                    String productName = resultSet.getString(5);
                    long ownerId = resultSet.getLong(6);
                    String productOwnerName = resultSet.getString(7);

                    FullOffer fullOffer = new FullOffer(
                            offerId,
                            labWorkId,
                            labWorkName,
                            productId,
                            productName,
                            ownerId,
                            productOwnerName,
                            OfferStatus.OPEN
                    );
                    userOffers.add(fullOffer);
                }
            }
        }
        return userOffers;
    }

    public List<FullOffer> selectOffersByProductOwner(Long userId) throws SQLException {
        List<FullOffer> userOffers = new ArrayList<>();

        try (PreparedStatement pstmt = connection.prepareStatement(SELECT_OFFERS_BY_PRODUCT_OWNER)){

            pstmt.setLong(1, userId);

            try(ResultSet resultSet = pstmt.executeQuery()){
                while (resultSet.next()){
                    long offerId = resultSet.getLong(1);
                    long labWorkId = resultSet.getLong(2);
                    String labWorkName = resultSet.getString(3);
                    long productId = resultSet.getLong(4);
                    String productName = resultSet.getString(5);
                    long ownerId = resultSet.getLong(6);
                    String labWorkOwnerName = resultSet.getString(7);

                    FullOffer fullOffer = new FullOffer(
                            offerId,
                            labWorkId,
                            labWorkName,
                            productId,
                            productName,
                            ownerId,
                            labWorkOwnerName,
                            OfferStatus.OPEN
                    );
                    userOffers.add(fullOffer);
                }
            }
        }
        return userOffers;
    }

    public FullOffer selectOffersById(long offerId) throws SQLException {

        try (PreparedStatement pstmt = connection.prepareStatement(SELECT_OFFER_BY_ID)){

            pstmt.setLong(1, offerId);

            try(ResultSet resultSet = pstmt.executeQuery()){
                if (resultSet.next()){
                    long labWorkId = resultSet.getLong(2);
                    String labWorkName = resultSet.getString(3);
                    long productId = resultSet.getLong(4);
                    String productName = resultSet.getString(5);
                    long ownerId = resultSet.getLong(6);
                    String labWorkOwnerName = resultSet.getString(7);
                    String offerStatus = resultSet.getString(8);

                    FullOffer fullOffer = new FullOffer(
                            offerId,
                            labWorkId,
                            labWorkName,
                            productId,
                            productName,
                            ownerId,
                            labWorkOwnerName,
                            OfferStatus.valueOf(offerStatus)
                    );
                    return fullOffer;
                }
            }
        }
        return null;
    }

    public void updateOffer(Offer offerToSave) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(UPDATE_OFFER)) {

            pstmt.setLong(1, offerToSave.getLabWorkId());
            pstmt.setLong(2, offerToSave.getProductId());
            pstmt.setString(3, offerToSave.getStatus().name());
            pstmt.setLong(4, offerToSave.getId());

            pstmt.executeUpdate();
        }
    }

    public List<Offer> selectOffersByProductId(Long productId) throws SQLException {
        List<Offer> offers = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(SELECT_OFFERS_BY_PRODUCT_ID)){

            pstmt.setLong(1, productId);

            try(ResultSet resultSet = pstmt.executeQuery()){
                while (resultSet.next()){
                    long id = resultSet.getLong(1);
                    long labWorkId = resultSet.getLong(2);
                    //long productId = resultSet.getLong(3);
                    String offerStatus = resultSet.getString(4);

                    Offer offer = new Offer(labWorkId, productId, OfferStatus.valueOf(offerStatus), id);
                    offers.add(offer);
                }
            }
        }
        return offers;
    }

    public List<Offer> selectOffersByLabWorkId(Long labWorkId) throws SQLException {
        List<Offer> offers = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(SELECT_OFFERS_BY_LAB_WORK_ID)){

            pstmt.setLong(1, labWorkId);

            try(ResultSet resultSet = pstmt.executeQuery()){
                while (resultSet.next()){
                    long id = resultSet.getLong(1);
                    //long labWorkId = resultSet.getLong(2);
                    long productId = resultSet.getLong(3);
                    String offerStatus = resultSet.getString(4);

                    Offer offer = new Offer(labWorkId, labWorkId, OfferStatus.valueOf(offerStatus), id);
                    offers.add(offer);
                }
            }
        }
        return offers;
    }
}


