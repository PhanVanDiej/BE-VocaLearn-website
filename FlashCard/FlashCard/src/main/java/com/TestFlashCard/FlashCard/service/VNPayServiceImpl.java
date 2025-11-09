package com.TestFlashCard.FlashCard.service;

import com.TestFlashCard.FlashCard.Utils.VNPayUtil;
import com.TestFlashCard.FlashCard.entity.PaymentTransaction;
import com.TestFlashCard.FlashCard.repository.PaymentTransactionRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class VNPayServiceImpl {

    @Value("${vnpay.tmnCode}")
    private String vnp_TmnCode;

    @Value("${vnpay.hashSecret}")
    private String secretKey;

    @Value("${vnpay.url}")
    private String vnp_PayUrl;

    @Value("${vnpay.returnUrl}")
    private String vnp_ReturnUrl;

    private final PaymentTransactionRepository paymentRepo;

    public VNPayServiceImpl(PaymentTransactionRepository paymentRepo) {
        this.paymentRepo = paymentRepo;
    }

    public String createPayment(Long amount, String orderInfo, String clientIp) throws UnsupportedEncodingException {
        // B1: Tạo PaymentTransaction trong DB
        PaymentTransaction transaction = new PaymentTransaction();
        String orderId = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 20);
        transaction.setOrderId(orderId);
        transaction.setAmount(amount);
        transaction.setCurrency("VND");
        transaction.setPaymentMethod("VNPAY");
        transaction.setTransactionStatus("PENDING");
        transaction.setDescription(orderInfo);
        transaction.setTransactionDate(LocalDateTime.now());
        paymentRepo.save(transaction);

        // B2: Tạo tham số gửi sang VNPAY
        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", vnp_TmnCode);
        vnpParams.put("vnp_Amount", String.valueOf(amount * 100));
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", transaction.getOrderId());
        vnpParams.put("vnp_OrderInfo", VNPayUtil.removeVietnameseAccent(orderInfo));
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", vnp_ReturnUrl);
        vnpParams.put("vnp_IpAddr", clientIp);
        // vnpParams.put("vnp_IpnUrl",
        // "https://eleven-poets-lick.loca.lt/api/payment/vnpay-ipn");
        // vnpParams.put("vnp_SecureHashType", "HmacSHA512");
        // Thời gian tạo và hết hạn
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        vnpParams.put("vnp_CreateDate", now.format(formatter));
        vnpParams.put("vnp_ExpireDate", now.plusMinutes(15).format(formatter));

        // B3: Sinh URL
        return VNPayUtil.getPaymentUrlLikeVnPaySample(vnpParams, secretKey, vnp_PayUrl);
    }

    public String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // Nếu có nhiều IP (trường hợp dùng proxy), lấy IP đầu tiên
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}