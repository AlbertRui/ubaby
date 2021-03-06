package com.ubaby.service;

import com.github.pagehelper.PageInfo;
import com.ubaby.common.ServerResponse;
import com.ubaby.pojo.Product;
import com.ubaby.vo.ProductDetail;
import com.ubaby.vo.ProductList;

/**
 * @author AlbertRui
 * @date 2018-05-06 21:16
 */
@SuppressWarnings("JavaDoc")
public interface ProductService {

    /**
     * 更新或新增产品
     *
     * @param product
     * @return
     */
    ServerResponse<String> saveOrUpdateProduct(Product product);

    /**
     * 产品上下架
     *
     * @param productId
     * @param status
     * @return
     */
    ServerResponse<String> setSaleStatus(Integer productId, Integer status);

    /**
     * 商品详细信息管理
     *
     * @param productId
     * @return
     */
    ServerResponse<ProductDetail> manageProductDetail(Integer productId);

    /**
     * 获取商品列表
     *
     * @param pageNum
     * @param pageSize
     * @return
     */
    ServerResponse<PageInfo<ProductList>> getProductList(int pageNum, int pageSize);

    /**
     * 后台商品搜索
     *
     * @param productName
     * @param productId
     * @param pageNum
     * @param pageSize
     * @return
     */
    ServerResponse<PageInfo<ProductList>> searchProduct(String productName, Integer productId, int pageNum, int pageSize);

    /**
     * 前台获取商品详细信息
     *
     * @param productId
     * @return
     */
    ServerResponse<ProductDetail> getProductDetail(Integer productId);

    /**
     * 前台根据关键字和商品分类获取商品
     *
     * @param keyWord
     * @param categoryId
     * @param pageNum
     * @param pageSize
     * @return
     */
    ServerResponse<PageInfo<ProductList>> getProductByKeyWordCategory(String keyWord, Integer categoryId, int pageNum, int pageSize, String orderBy);

}
