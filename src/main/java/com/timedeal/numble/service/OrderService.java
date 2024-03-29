package com.timedeal.numble.service;

import com.timedeal.numble.controller.error.CustomException;
import com.timedeal.numble.controller.error.ErrorCode;
import com.timedeal.numble.controller.order.Order;
import com.timedeal.numble.controller.order.OrderSaveRequest;
import com.timedeal.numble.entity.OrderEntity;
import com.timedeal.numble.entity.ProductEntity;
import com.timedeal.numble.entity.UserEntity;
import com.timedeal.numble.repository.OrderRepository;
import com.timedeal.numble.repository.ProductRepository;
import com.timedeal.numble.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    /**
     * 구매하기 (Pessimistic Lock)
     */
    @Transactional
    public Long addOrderWithPessimisticLock(String loginId, OrderSaveRequest request) {
        // 유저 존재여부 확인
        UserEntity userEntity = userRepository.findByLoginId(loginId)
                .map(user -> {
                    // 중복 주문여부 확인
                    orderRepository.findByUserIdAndProductId(user.getId(), request.getProductId())
                            .ifPresent(order -> {
                                throw new CustomException(ErrorCode.DUPLICATED_ORDER);
                            });
                    return user;
                })
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 상품 조회
        return productRepository.findByIdWithPessimisticLock(request.getProductId())
                .map(productEntity -> {
                    // 상품 재고 체크
                    if (productEntity.isSoldOut()) {
                        throw new CustomException(ErrorCode.PRODUCT_SOLD_OUT);
                    }
                    // 주문 가능 시간 여부 체크
                    if (!productEntity.isSaleTime()) {
                        throw new CustomException(ErrorCode.NOT_SALE_TIME);
                    }

                    // 주문 등록
                    OrderEntity savedOrder = orderRepository.save(OrderEntity.create(userEntity, productEntity));
                    return savedOrder.getId();
                })
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
    }


    /**
     * 구매하기 (Optimistic Lock)
     */
    @Transactional
    public Long addOrderWithOptimisticLock(String loginId, OrderSaveRequest request) {
        // 유저 존재여부 확인
        UserEntity userEntity = userRepository.findByLoginId(loginId)
                .map(user -> {
                    // 중복 주문여부 확인
                    orderRepository.findByUserIdAndProductId(user.getId(), request.getProductId())
                            .ifPresent(order -> {
                                throw new CustomException(ErrorCode.DUPLICATED_ORDER);
                            });
                    return user;
                })
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 상품 조회
        return productRepository.findByWithOptimisticLock(request.getProductId())
                .map(productEntity -> {
                    // 상품 재고 체크
                    if (productEntity.isSoldOut()) {
                        throw new CustomException(ErrorCode.PRODUCT_SOLD_OUT);
                    }
                    // 주문 가능 시간 여부 체크
                    if (!productEntity.isSaleTime()) {
                        throw new CustomException(ErrorCode.NOT_SALE_TIME);
                    }

                    // 주문 등록
                    OrderEntity savedOrder = orderRepository.save(OrderEntity.create(userEntity, productEntity));
                    return savedOrder.getId();
                })
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
    }


    /**
     * 구매하기 (Synchronized)
     */
    @Transactional
    public synchronized Long addOrderWithSynchronized(String loginId, OrderSaveRequest request) {
        // 유저 존재여부 확인
        UserEntity userEntity = userRepository.findByLoginId(loginId)
                .map(user -> {
                    // 중복 주문여부 확인
                    orderRepository.findByUserIdAndProductId(user.getId(), request.getProductId())
                            .ifPresent(order -> {
                                throw new CustomException(ErrorCode.DUPLICATED_ORDER);
                            });
                    return user;
                })
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 상품 조회
        return productRepository.findById(request.getProductId())
                .map(productEntity -> {
                    // 상품 재고 체크
                    if (productEntity.isSoldOut()) {
                        throw new CustomException(ErrorCode.PRODUCT_SOLD_OUT);
                    }
                    // 주문 가능 시간 여부 체크
                    if (!productEntity.isSaleTime()) {
                        throw new CustomException(ErrorCode.NOT_SALE_TIME);
                    }

                    // 주문 등록
                    OrderEntity savedOrder = orderRepository.saveAndFlush(OrderEntity.create(userEntity, productEntity));
                    return savedOrder.getId();
                })
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    /**
     * 구매하기
     */
    @Transactional
    public Long addOrder(String loginId, OrderSaveRequest request) {
        // 유저 존재여부 확인
        UserEntity userEntity = userRepository.findByLoginId(loginId)
                .map(user -> {
                    // 중복 주문여부 확인
                    orderRepository.findByUserIdAndProductId(user.getId(), request.getProductId())
                            .ifPresent(order -> {
                                throw new CustomException(ErrorCode.DUPLICATED_ORDER);
                            });
                    return user;
                })
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 상품 조회
        return productRepository.findById(request.getProductId())
                .map(productEntity -> {
                    // 상품 재고 체크
                    if (productEntity.isSoldOut()) {
                        throw new CustomException(ErrorCode.PRODUCT_SOLD_OUT);
                    }
                    // 주문 가능 시간 여부 체크
                    if (!productEntity.isSaleTime()) {
                        throw new CustomException(ErrorCode.NOT_SALE_TIME);
                    }

                    // 주문 등록
                    OrderEntity savedOrder = orderRepository.saveAndFlush(OrderEntity.create(userEntity, productEntity));
                    return savedOrder.getId();
                })
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
    }


    /**
     * 구매 내역 단건 조회
     */
    @Transactional(readOnly = true)
    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .map(Order::fromOrderEntity)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
    }

    /**
     * 유저의 구매한 상품 리스트 보기
     */
    @Transactional(readOnly = true)
    public Page<Order> getMemberOrders(String loginId, Pageable pageable) {
        // 유저 존재여부 확인
        UserEntity userEntity = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return orderRepository.findByUserId(userEntity.getId(), pageable)
                .map(Order::fromOrderEntity);
    }

    /**
     * 상품별 구매한 유저리스트 조회
     */
    @Transactional(readOnly = true)
    public Page<Order> getProductOrders(Long productId, Pageable pageable) {
        // 상품 존재여부 확인
        ProductEntity productEntity = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        return orderRepository.findByProductId(productEntity.getId(), pageable)
                .map(Order::fromOrderEntity);
    }

    /**
     * 구매 취소
     */
    @Transactional
    public void cancelOrder(Long orderId) {
        orderRepository.findById(orderId)
                .ifPresentOrElse(orderEntity -> {
                    // 재고량 다시 추가
                    orderEntity.getProduct().addQuantity(1); // 하드코딩 주문수량 1
                    // 주문 삭제
                    orderRepository.delete(orderEntity);
                }, () -> {
                    throw new CustomException(ErrorCode.ORDER_NOT_FOUND);
                });
    }

}
