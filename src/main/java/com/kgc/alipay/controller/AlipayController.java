package com.kgc.alipay.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.kgc.alipay.constant.AlipayConfig;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 支付宝支付
 */
@Controller
@RequestMapping("/Alipay")
public class AlipayController {

    /**
     * 生成订单直接跳转支付宝付款
     */
    @RequestMapping("/to_alipay.do")
    public void toAlipay(HttpServletResponse response, HttpServletRequest request) throws Exception{

        AlipayClient alipayClient = new DefaultAlipayClient(
                AlipayConfig.gatewayUrl, AlipayConfig.app_id,
                AlipayConfig.merchant_private_key, "json", AlipayConfig.charset,
                AlipayConfig.alipay_public_key, AlipayConfig.sign_type);

        // 取购买人名称
        String in_name = request.getParameter("in_name");
        // 取手机号
        String in_phone = request.getParameter("in_phone");
        // 创建唯一订单号
        int random = (int) (Math.random() * 10000);
        String dateStr = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

        // 订单号拼接规则：手机号后四位+当前时间后四位+随机数四位数
        String out_trade_no = in_phone.substring(7) + dateStr.substring(10)
                + random;
        // 拼接订单名称
        String subject = in_name + "的订单";

        // 取付款金额
        String total_amount = request.getParameter("in_money");

        // 设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(AlipayConfig.return_url);//支付成功响应后跳转地址
        alipayRequest.setNotifyUrl(AlipayConfig.notify_url);//异步请求地址

        /*FAST_INSTANT_TRADE_PAY 二维码瞬时支付
         * out_trade_no 订单号 total_amount 订单金额  subject 订单名称
         */
        alipayRequest.setBizContent("{\"out_trade_no\":\"" + out_trade_no
                + "\"," + "\"total_amount\":\"" + total_amount + "\","
                + "\"subject\":\"" + subject + "\"," + "\"body\":\""
                + ""+ "\"," + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");
        String result = "请求无响应";
        // 请求
        try {
            //通过阿里客户端，发送支付页面请求
            result = alipayClient.pageExecute(alipayRequest).getBody();
            response.setContentType("text/html;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().println(result);
            response.getWriter().flush();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        } finally {
            response.getWriter().close();
        }
    }


    /**
     * 支付成功后处理业务
     */
    @RequestMapping("/alipay_return.do")
    public String alipayReturn(HttpServletRequest request, Map<String, Object> map) throws Exception{

        // 响应信息
        String msg = "";

        // 请在这里编写您的程序（以下代码仅作参考）
        if (verifyAlipayReturn(request)) {//验签成功后执行的自定义业务代码
            // 商户订单号
            String out_trade_no = new String(request.getParameter(
                    "out_trade_no").getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);

            // 支付宝交易号
            String trade_no = new String(request.getParameter("trade_no")
                    .getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);

            // 付款金额
            String total_amount = new String(request.getParameter(
                    "total_amount").getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
            msg = "支付宝交易号:" + trade_no + "<br/>商户订单号"
                    + out_trade_no + "<br/>付款金额:" + total_amount;

        } else {
            msg = "验签/支付失败";
        }

        map.put("msg", msg);

        return "forward:/success.jsp";
    }

    /**
     * 支付宝异步通知
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping("/notify_url.do")
    public void alipayNotify(HttpServletRequest request,HttpServletResponse response)
                                                throws Exception {
        // ——请在这里编写您的程序（以下代码仅作参考）——

        /*
         * 实际验证过程建议商户务必添加以下校验： 1、需要验证该通知数据中的out_trade_no是否为商户系统中创建的订单号，
         * 2、判断total_amount是否确实为该订单的实际金额（即商户订单创建时的金额），
         * 3、校验通知中的seller_id（或者seller_email)
         * 是否为out_trade_no这笔单据的对应的操作方（有的时候，一个商户可能有多个seller_id/seller_email）
         * 4、验证app_id是否为该商户本身。
         */
        if (verifyAlipayReturn(request)) {// 验证成功
            // 商户订单号
            String out_trade_no = new String(request.getParameter(
                    "out_trade_no").getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
            System.out.println(out_trade_no);
            // 支付宝交易号
            String trade_no = new String(request.getParameter("trade_no")
                    .getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
            System.out.println(trade_no);

            // 交易状态
            String trade_status = new String(request.getParameter(
                    "trade_status").getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);

            if (trade_status.equals("TRADE_FINISHED")) {
                // 判断该笔订单是否在商户网站中已经做过处理
                // 如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
                // 如果有做过处理，不执行商户的业务程序
                // 注意：
                // 退款日期超过可退款期限后（如三个月可退款），支付宝系统发送该交易状态通知
            } else if (trade_status.equals("TRADE_SUCCESS")) {
                // 判断该笔订单是否在商户网站中已经做过处理
                // 如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
                // 如果有做过处理，不执行商户的业务程序
                // 注意：
                // 付款完成后，支付宝系统发送该交易状态通知
            }

        } else {// 验证失败
            response.setContentType("text/html;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().println("验签/支付失败！");
            response.getWriter().flush();
            response.getWriter().close();

            // 调试用，写文本函数记录程序运行情况是否正常
            // String sWord = AlipaySignature.getSignCheckContentV1(params);
            // AlipayConfig.logResult(sWord);
        }
    }

    /**
     * @author zhukang
     * @date 2021-04-23
     * @return
     * @description 验证支付宝的反馈信息
     */
    private boolean verifyAlipayReturn(HttpServletRequest request) throws UnsupportedEncodingException {
        // 获取支付宝回调反馈的信息
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (Iterator<String> iter = requestParams.keySet().iterator(); iter
                .hasNext();) {
            String name = iter.next();
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            // 乱码解决，这段代码在出现乱码时使用
            valueStr = new String(valueStr.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
            params.put(name, valueStr);
        }

        boolean signVerified = false;
        try {// 调用SDK验证签名
            signVerified = AlipaySignature.rsaCheckV1(params,
                    AlipayConfig.alipay_public_key, AlipayConfig.charset,
                    AlipayConfig.sign_type);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        return signVerified;
    }
}
