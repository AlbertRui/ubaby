package com.ubaby.controller.backend;

import com.google.common.collect.Maps;
import com.ubaby.common.Const;
import com.ubaby.common.ResponseCode;
import com.ubaby.common.ServerResponse;
import com.ubaby.pojo.Product;
import com.ubaby.pojo.User;
import com.ubaby.service.FileService;
import com.ubaby.service.ProductService;
import com.ubaby.service.UserService;
import com.ubaby.util.PropertiesUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * @author AlbertRui
 * @date 2018-05-06 21:06
 */
@SuppressWarnings("JavaDoc")
@RequestMapping("/manage/product/")
@Controller
public class ProductManageController {

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private FileService fileService;

    @RequestMapping("save.do")
    @ResponseBody
    public ServerResponse productSave(HttpSession session, Product product) {

        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null)
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "请您以管理员身份登录");

        if (userService.checkAdminRole(user).isSuccess())
            return productService.saveOrUpdateProduct(product);

        return ServerResponse.createByErrorMessage("无权限操作");

    }

    @RequestMapping("set_sale_status.do")
    @ResponseBody
    public ServerResponse setSaleStatus(HttpSession session, Integer productId, Integer status) {

        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null)
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "请您以管理员身份登录");

        if (userService.checkAdminRole(user).isSuccess())
            return productService.setSaleStatus(productId, status);

        return ServerResponse.createByErrorMessage("无权限操作");

    }

    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse getDetail(HttpSession session, Integer productId) {

        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null)
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "请您以管理员身份登录");

        if (userService.checkAdminRole(user).isSuccess())
            return productService.manageProductDetail(productId);

        return ServerResponse.createByErrorMessage("无权限操作");

    }

    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse getList(HttpSession session, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {

        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null)
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "请您以管理员身份登录");

        if (userService.checkAdminRole(user).isSuccess())
            return productService.getProductList(pageNum, pageSize);

        return ServerResponse.createByErrorMessage("无权限操作");

    }

    @RequestMapping("search.do")
    @ResponseBody
    public ServerResponse searchProduct(HttpSession session, String productName, Integer productId, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {

        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null)
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "请您以管理员身份登录");

        if (userService.checkAdminRole(user).isSuccess())
            return productService.searchProduct(productName, productId, pageNum, pageSize);

        return ServerResponse.createByErrorMessage("无权限操作");

    }

    @RequestMapping("upload.do")
    @ResponseBody
    public ServerResponse upload(HttpSession session, @RequestParam(value = "uploadFile", required = false) MultipartFile file, HttpServletRequest request) {

        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null)
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "请您以管理员身份登录");

        if (userService.checkAdminRole(user).isSuccess()) {

            String path = request.getSession().getServletContext().getRealPath("upload");
            String targetFileName = fileService.upload(file, path);
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFileName;

            Map<String, String> fileMap = Maps.newHashMap();
            fileMap.put("uri", targetFileName);
            fileMap.put("url", url);

            return ServerResponse.createBySuccess(fileMap);

        }

        return ServerResponse.createByErrorMessage("无权限操作");

    }

    /**
     * 富文本中对于返回值有自己的要求,我们使用是simditor所以按照simditor的要求进行返回
     *
     * @param session
     * @param file
     * @param request
     * @return
     */
    @RequestMapping("rich_text_upload.do")
    @ResponseBody
    public Map<String, Object> richTextUpload(HttpSession session, @RequestParam(value = "uploadFile", required = false) MultipartFile file, HttpServletRequest request, HttpServletResponse response) {

        Map<String, Object> resultMap = Maps.newHashMap();
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            resultMap.put("success", false);
            resultMap.put("msg", "请您以管理员身份登录");
            return resultMap;
        }

        if (userService.checkAdminRole(user).isSuccess()) {

            String path = request.getSession().getServletContext().getRealPath("upload");
            String targetFileName = fileService.upload(file, path);
            if (StringUtils.isBlank(targetFileName)) {
                resultMap.put("success", false);
                resultMap.put("msg", "上传失败");
                return resultMap;
            }

            String url = PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFileName;
            resultMap.put("success",true);
            resultMap.put("msg","上传成功");
            resultMap.put("file_path",url);
            response.addHeader("Access-Control-Allow-Headers","X-File-Name");
            return resultMap;

        }

        resultMap.put("success", false);
        resultMap.put("msg", "无权限操作");
        return resultMap;

    }

}
