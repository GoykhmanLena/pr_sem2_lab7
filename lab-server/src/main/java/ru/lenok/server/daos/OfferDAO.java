package ru.lenok.server.daos;

import ru.lenok.common.FullOffer;
import ru.lenok.common.Offer;
import ru.lenok.common.OfferStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static ru.lenok.server.daos.SQLQueries.*;

public class OfferDAO extends AbstractDAO {

    public OfferDAO(DBConnector dbConnector, boolean dbReinit) throws SQLException {
        super(dbConnector.getDatasource());
        init(dbReinit);
    }

    private void init(boolean dbReinit) throws SQLException {
        initScheme(dbReinit);
    }

    private void initScheme(boolean reinitDB) throws SQLException {
        try (Connection connection = ds.getConnection(); Statement stmt = connection.createStatement()) {
            if (reinitDB) {
                stmt.executeUpdate(DROP_ALL_OFFER.t());
            }
            stmt.executeUpdate(CREATE_SEQUENCE_OFFER.t());
            stmt.executeUpdate(CREATE_TABLE_OFFER.t());
        }
    }

    public Offer insert(Offer offer) throws SQLException {
        try (Connection connection = ds.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(CREATE_OFFER.t())) {

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
        try (Connection connection = ds.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(SELECT_OFFERS_BY_LAB_WORK_OWNER.t())) {
            pstmt.setLong(1, userId);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                while (resultSet.next()) {
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
        try (Connection connection = ds.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(SELECT_OFFERS_BY_PRODUCT_OWNER.t())) {

            pstmt.setLong(1, userId);

            try (ResultSet resultSet = pstmt.executeQuery()) {
                while (resultSet.next()) {
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
        try (Connection connection = ds.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(SELECT_OFFER_BY_ID.t())) {
            pstmt.setLong(1, offerId);

            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
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

    public void updateOffer(Offer offerToSave, Connection connection) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(UPDATE_OFFER.t())) {
            pstmt.setLong(1, offerToSave.getLabWorkId());
            pstmt.setLong(2, offerToSave.getProductId());
            pstmt.setString(3, offerToSave.getStatus().name());
            pstmt.setLong(4, offerToSave.getId());
            pstmt.executeUpdate();
        }
    }

    public List<Offer> selectOffersByProductId(Long productId) throws SQLException {
        List<Offer> offers = new ArrayList<>();
        try (Connection connection = ds.getConnection(); PreparedStatement pstmt = connection.prepareStatement(SELECT_OFFERS_BY_PRODUCT_ID.t())) {
            pstmt.setLong(1, productId);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                while (resultSet.next()) {
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
        try (Connection connection = ds.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(SELECT_OFFERS_BY_LAB_WORK_ID.t())) {
            pstmt.setLong(1, labWorkId);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                while (resultSet.next()) {
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
