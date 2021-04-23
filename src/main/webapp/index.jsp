<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
    <title>订单页</title>
</head>

<body>
<!--商品信息列表-->
<table border="1px" align="center" width="60%" cellspacing="0px">
    <tr>
        <td colspan="4">
            用户名：xiaokang
        </td>
    </tr>
    <tr>
        <td>商品信息</td>
        <td>单价(￥)</td>
        <td>数量</td>
        <td>金额（￥）</td>
    </tr>
    <tr>
        <td>华为Meta20<br/><img src="img/meta20.png" width="200px" height="200px" /></td>
        <td>20.00</td>
        <td>2</td>
        <td>40.00</td>
    </tr>
    <tr>
        <td colspan="3">
            <span> 总金额(￥)： </span> <span class="totalPrice">40.00</span>
        </td>
        <td><input type="button" value="提交订单" id="submit_prod"></td>
    </tr>

</table>
</body>
<script type="text/javascript" src="js/jquery-1.12.4.js"></script>
<script type="text/javascript">
    $(function(){
        $("#submit_prod").click(function() {
            var name = "zuker";
            var phone = "15250954865";
            var money = $(".totalPrice").text();
            window.location.href = "Alipay/to_alipay.do?in_name=" + name
                + "&&in_phone=" + phone + "&&in_money=" + money;
        });
    })
    //提交订单

</script>
</html>
