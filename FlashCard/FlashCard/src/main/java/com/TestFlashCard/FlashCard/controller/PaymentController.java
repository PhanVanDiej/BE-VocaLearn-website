package com.TestFlashCard.FlashCard.controller;

import com.TestFlashCard.FlashCard.Utils.VNPayUtil;
import com.TestFlashCard.FlashCard.response.ApiResponse;
import com.TestFlashCard.FlashCard.service.VNPayServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final VNPayServiceImpl vnPayService;
    @Value("${vnpay.hashSecret}")
    private String vnp_HashSecret;

    public PaymentController(VNPayServiceImpl vnPayService) {
        this.vnPayService = vnPayService;
    }

    @PostMapping("/vnpay/create")
    public ApiResponse<?> createVnpayPayment(@RequestParam Long amount,
                                             @RequestParam String description, HttpServletRequest request) throws Exception {
        try{
            String clientIp = vnPayService.getClientIp(request);
            return new ApiResponse<>(HttpStatus.ACCEPTED.value(), "Trả về url thành công",vnPayService.createPayment(amount, description,clientIp));
        }catch(Exception e){
            return new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), "Trả về url thất bại vì " + e.getMessage());
        }

    }


    @GetMapping("/vnpay-ipn")
    public ApiResponse<Map<String, String>> handleVnpayIPN(HttpServletRequest request) {
        Map<String, String> response = new HashMap<>();
        try {
            // B1: Lấy toàn bộ tham số gửi về từ VNPAY
            Map<String, String> fields = new HashMap<>();
            Enumeration<String> params = request.getParameterNames();
            while (params.hasMoreElements()) {
                String fieldName = params.nextElement();
                String fieldValue = request.getParameter(fieldName);
                if (fieldValue != null && fieldValue.length() > 0) {
                    fields.put(fieldName, fieldValue);
                }
            }

            // B2: Lấy chữ ký từ VNPAY
            String vnp_SecureHash = fields.remove("vnp_SecureHash");
            fields.remove("vnp_SecureHashType");

            // B3: Ký lại dữ liệu để so sánh
            String signValue = VNPayUtil.hashAllFields(fields, vnp_HashSecret);

            if (signValue.equals(vnp_SecureHash)) {
                // ✅ Check chữ ký hợp lệ
                String vnp_TxnRef = fields.get("vnp_TxnRef");
                String vnp_ResponseCode = fields.get("vnp_ResponseCode");
                String vnp_Amount = fields.get("vnp_Amount");

                // TODO: kiểm tra dữ liệu thực trong DB
                boolean checkOrderId = true;
                boolean checkAmount = true;
                boolean checkOrderStatus = true;

                if (checkOrderId) {
                    if (checkAmount) {
                        if (checkOrderStatus) {
                            if ("00".equals(vnp_ResponseCode)) {
                                response.put("RspCode", "00");
                                response.put("Message", "Confirm Success");
                            } else {
                                response.put("RspCode", "00");
                                response.put("Message", "Payment Failed");
                            }
                        } else {
                            response.put("RspCode", "02");
                            response.put("Message", "Order already confirmed");
                        }
                    } else {
                        response.put("RspCode", "04");
                        response.put("Message", "Invalid Amount");
                    }
                } else {
                    response.put("RspCode", "01");
                    response.put("Message", "Order not Found");
                }
            } else {
                // ❌ Sai chữ ký
                response.put("RspCode", "97");
                response.put("Message", "Invalid Checksum");
            }

            return new ApiResponse<>(HttpStatus.OK.value(), "Trả về data thành công", response);

        } catch (Exception e) {
            response.put("RspCode", "99");
            response.put("Message", "Unknown error");
            return new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), "Trả về data thất bại", response);
        }
    }
    @GetMapping("/vnpay-return")
    public ResponseEntity<String> handleVnpayReturn(HttpServletRequest request) throws UnsupportedEncodingException {
        Map<String, String> fields = new HashMap<>();

        // ✅ Lấy tất cả các tham số VNPAY gửi về
        Map<String, String[]> paramMap = request.getParameterMap();
        for (String key : paramMap.keySet()) {
            String[] values = paramMap.get(key);
            if (values != null && values.length > 0 && !values[0].isEmpty()) {
                fields.put(key, values[0]);
            }
        }

        // ✅ Lấy SecureHash để kiểm tra
        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");
        fields.remove("vnp_SecureHash");

        // ✅ Tính chữ ký (hashAllFields là hàm bạn đã có trong VNPayUtil)
        String signValue = VNPayUtil.hashAllFields(fields,vnp_HashSecret);

        // ✅ Tạo HTML trả về
        StringBuilder html = new StringBuilder("<html><body style='font-family:sans-serif;'>");

        if (signValue.equals(vnp_SecureHash)) {
            String vnp_ResponseCode = request.getParameter("vnp_ResponseCode");

            if ("00".equals(vnp_ResponseCode)) {
                html.append("<h2 style='color:green;'>Giao dịch thành công!</h2>");
            } else {
                html.append("<h2 style='color:red;'>Giao dịch thất bại!</h2>")
                        .append("<p>Mã lỗi: ").append(vnp_ResponseCode).append("</p>");
            }

        } else {
            html.append("<h2 style='color:red;'>Chữ ký không hợp lệ!</h2>");
        }

        html.append("<br><a href='/'>Quay lại trang chủ</a></body></html>");

        return ResponseEntity.ok()
                .header("Content-Type", "text/html; charset=UTF-8")
                .body(html.toString());
    }
}