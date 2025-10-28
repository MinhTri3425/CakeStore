package com.cakestore.cakestore.controller.user;

import com.cakestore.cakestore.entity.Order;
import com.cakestore.cakestore.entity.User;
import com.cakestore.cakestore.repository.UserRepository;
import com.cakestore.cakestore.repository.user.OrderRepository;
import com.cakestore.cakestore.service.user.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderHistoryController {

    private final OrderRepository orderRepo;
    private final UserRepository userRepo;
    private final InventoryService inventoryService;

    @GetMapping
    public String listOrders(Authentication auth, Model model) {
        // bắt buộc login
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login?next=/orders";
        }

        // lấy user hiện tại
        User currentUser = userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // lấy danh sách đơn + fetch branch (repo custom của bạn)
        List<Order> orders = orderRepo.findAllByUserIdFetchBranch(currentUser.getId());

        model.addAttribute("orders", orders);
        return "order/orders"; // templates/order/orders.html
    }

    @GetMapping("/{orderId}")
    public String orderDetail(
            @PathVariable Long orderId,
            Authentication auth,
            Model model) {
        // bắt buộc login
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login?next=/orders/" + orderId;
        }

        // lấy user hiện tại
        User currentUser = userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // lấy chi tiết đơn (fetch branch + address + items + product/variant)
        Order order = orderRepo.findByIdAndUserIdFetchAll(orderId, currentUser.getId())
                .orElse(null);

        // nếu không tìm thấy hoặc không thuộc user -> quay danh sách
        if (order == null) {
            return "redirect:/orders";
        }

        model.addAttribute("order", order);
        return "order/detail"; // templates/order/detail.html
    }

    @PostMapping("/{orderId}/cancel")
    @Transactional
    public String cancelOrder(
            @PathVariable Long orderId,
            Authentication auth) {
        // bắt login
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login?next=/orders/" + orderId;
        }

        // lấy user hiện tại
        User currentUser = userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // lấy đơn + mọi quan hệ cần thiết (branch, items, product,...)
        Order order = orderRepo.findByIdAndUserIdFetchAll(orderId, currentUser.getId())
                .orElse(null);

        if (order == null) {
            // không có đơn hoặc không thuộc user -> quay về lịch sử
            return "redirect:/orders";
        }

        // chỉ cho hủy nếu đơn còn NEW và chưa thanh toán
        boolean canCancel = order.getStatus() == Order.OrderStatus.NEW &&
                order.getPaymentStatus() == Order.PaymentStatus.UNPAID;

        if (!canCancel) {
            // không hợp lệ -> quay lại chi tiết, không đổi gì
            return "redirect:/orders/" + orderId;
        }

        // 1. Nhả lại hàng đã reserve (rất quan trọng)
        inventoryService.releaseReservedFromOrder(order);

        // 2. Cập nhật trạng thái đơn
        order.setStatus(Order.OrderStatus.CANCELED);

        // 3. Lưu lại đơn
        orderRepo.save(order);

        // xong -> quay lại chi tiết đơn
        return "redirect:/orders/" + orderId;
    }

}
