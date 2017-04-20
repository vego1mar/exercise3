package wdsr.exercise3.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import wdsr.exercise3.model.Product;
import wdsr.exercise3.model.ProductType;

public class ProductService extends RestClientBase {

    protected ProductService(final String serverHost, final int serverPort, final Client client) {
        super(serverHost, serverPort, client);
    }

    /**
     * Looks up all products of given types known to the server.
     * 
     * @param types Set of types to be looked up
     * @return A list of found products - possibly empty, never null.
     */
    public List<Product> retrieveProducts(Set<ProductType> types) {
        List<Product> resultListOfProducts = new ArrayList<Product>();
        WebTarget productsTarget = baseTarget.path("/products");
        Response serverResponse =
                productsTarget.request(MediaType.APPLICATION_JSON).get(Response.class);

        if (serverResponse.getStatus() != Response.Status.OK.getStatusCode()) {
            resultListOfProducts.clear();
            return resultListOfProducts;
        }

        List<Product> responseListOfProducts =
                serverResponse.readEntity(new GenericType<ArrayList<Product>>() {});

        for (Product product : responseListOfProducts) {
            if (types.contains(product.getType())) {
                resultListOfProducts.add(product);
            }
        }

        return resultListOfProducts;
    }

    /**
     * Looks up all products known to the server.
     * 
     * @return A list of all products - possibly empty, never null.
     */
    public List<Product> retrieveAllProducts() {
        List<Product> listOfProducts = new ArrayList<Product>();
        WebTarget productsTarget = baseTarget.path("/products");
        Response serverResponse =
                productsTarget.request(MediaType.APPLICATION_JSON).get(Response.class);

        if (serverResponse.getStatus() != Response.Status.OK.getStatusCode()) {
            listOfProducts.clear();
            return listOfProducts;
        }

        listOfProducts = serverResponse.readEntity(new GenericType<ArrayList<Product>>() {});
        return listOfProducts;
    }

    /**
     * Looks up the product for given ID on the server.
     * 
     * @param id Product ID assigned by the server
     * @return Product if found
     * @throws NotFoundException if no product found for the given ID.
     */
    public Product retrieveProduct(int id) {
        WebTarget productsTarget = baseTarget.path("/products");
        baseTarget.queryParam("id", id);
        Response serverResponse =
                productsTarget.request(MediaType.APPLICATION_JSON).get(Response.class);
        List<Product> responseProduct =
                serverResponse.readEntity(new GenericType<ArrayList<Product>>() {});

        if (responseProduct == null || responseProduct.size() == 0) {
            throw new NotFoundException();
        }

        return responseProduct.get(0);
    }

    /**
     * Creates a new product on the server.
     * 
     * @param product Product to be created. Must have null ID field.
     * @return ID of the new product.
     * @throws WebApplicationException if request to the server failed
     */
    public int storeNewProduct(Product product) {
        WebTarget productsTarget = baseTarget.path("/products");
        Response serverResponse =
                productsTarget.request().post(Entity.entity(product, MediaType.APPLICATION_JSON));

        if (serverResponse.getStatus() != Response.Status.CREATED.getStatusCode()) {
            throw new WebApplicationException();
        }

        // TODO: returning ID of the new product
        return 0;
    }

    /**
     * Updates the given product.
     * 
     * @param product Product with updated values. Its ID must identify an existing resource.
     * @throws NotFoundException if no product found for the given ID.
     */
    public void updateProduct(Product product) {
        WebTarget productsTarget = baseTarget.path("/products/" + product.getId());
        Response serverResponse =
                productsTarget.request().put(Entity.entity(product, MediaType.APPLICATION_JSON));

        if (serverResponse.getStatus() != Response.Status.NO_CONTENT.getStatusCode()) {
            throw new NotFoundException();
        }
    }

    /**
     * Deletes the given product.
     * 
     * @param product Product to be deleted. Its ID must identify an existing resource.
     * @throws NotFoundException if no product found for the given ID.
     */
    public void deleteProduct(Product product) {
        WebTarget productsTarget = baseTarget.path("/products/" + product.getId());
        Response serverResponse = productsTarget.request().delete();

        if (serverResponse.getStatus() != Response.Status.NO_CONTENT.getStatusCode()) {
            throw new NotFoundException();
        }
    }

}
