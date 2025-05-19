package ru.lenok.server.services;

import ru.lenok.common.FullOffer;
import ru.lenok.common.Offer;
import ru.lenok.common.OfferStatus;
import ru.lenok.common.Product;
import ru.lenok.common.models.LabWork;
import ru.lenok.server.collection.LabWorkService;
import ru.lenok.server.daos.DBConnector;
import ru.lenok.server.daos.LabWorkDAO;
import ru.lenok.server.daos.OfferDAO;
import ru.lenok.server.daos.ProductDAO;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class OfferService {
    private final LabWorkDAO labWorkDAO;
    private final ProductDAO productDAO;
    private final OfferDAO offerDAO;
    private final LabWorkService labWorkService;
    private final Connection connection;

    public OfferService(LabWorkDAO labWorkDAO, ProductDAO productDAO, OfferDAO offerDAO, LabWorkService labWorkService, DBConnector dbConnector) {
        this.labWorkDAO = labWorkDAO;
        this.productDAO = productDAO;
        this.offerDAO = offerDAO;
        this.labWorkService = labWorkService;
        connection = dbConnector.getConnection();
    }


    public void createOffer(long labWorkId, long productId, Long userId) throws SQLException {
        Offer offer = new Offer(labWorkId, productId, OfferStatus.OPEN, null);
        Product productFromDB = productDAO.getProductById(productId);
        if (productFromDB == null){
            throw new IllegalArgumentException("Продукта с таким id не существует, проверьте еще раз");
        }
        if (!productFromDB.getOwnerId().equals(userId)){
            throw new IllegalArgumentException("За вами едет полиция!!! Попытка обмена на продукт, не принадлежащий вам: " + productFromDB.getName());
        }
        try {
            offerDAO.insert(offer);
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }

    public List<FullOffer> getIncomingOffers(Long userId) throws SQLException {
        return offerDAO.selectOffersByLabWorkOwner(userId);
    }

    public List<FullOffer> getOutgoingOffers(Long userId) throws SQLException {
        return offerDAO.selectOffersByProductOwner(userId);
    }

    public void acceptOffer(long offerId, Long userId) throws SQLException {
        FullOffer offer = offerDAO.selectOffersById(offerId);
        if (offer == null) {
            throw new IllegalArgumentException("Предложения с таким id не существует " + offerId);
        }
        if (!offer.getOwnerId().equals(userId)) {
            throw new IllegalArgumentException(String.format("LabWork %s (с id = %d) в предложении id = %d вам не принадлежит", offer.getLabWorkName(), offer.getLabWorkId(), offer.getId()));
        }
        Product product = productDAO.getProductById(offer.getProductId());
        Long productOwnerId = product.getOwnerId();

        LabWork labWork = labWorkService.getLabWorkById(offer.getLabWorkId());

        try {
            labWork.setOwnerId(productOwnerId);
            labWorkService.updateByLabWorkId(labWork.getId(), labWork, false);

            product.setOwnerId(userId);
            productDAO.updateProduct(product);

            Offer offerToSave = new Offer(offer.getLabWorkId(), offer.getProductId(), OfferStatus.CLOSE, offer.getId());
            offerDAO.updateOffer(offerToSave);

            List<Offer> offersByProductId = offerDAO.selectOffersByProductId(offer.getProductId());
            cancelOffersExceptGiven(offerId, offersByProductId);

            List<Offer> selectOffersByLabWorkId = offerDAO.selectOffersByLabWorkId(offer.getLabWorkId());
            cancelOffersExceptGiven(offerId, selectOffersByLabWorkId);

            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            labWork.setOwnerId(userId);
            throw e;
        }
    }

    private void cancelOffersExceptGiven(long offerId, List<Offer> offers) throws SQLException {
        for (Offer o : offers) {
            if (!o.getId().equals(offerId)) {
                o.setStatus(OfferStatus.CANCEL);
                offerDAO.updateOffer(o);
            }
        }
    }
}
