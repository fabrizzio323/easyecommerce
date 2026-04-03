package com.fabrizio.easyecommerce.Service;

import com.fabrizio.easyecommerce.dto.CartItemRequestDTO;
import com.fabrizio.easyecommerce.dto.CartResponseDTO;
import com.fabrizio.easyecommerce.entity.Cart;
import com.fabrizio.easyecommerce.entity.CartItem;
import com.fabrizio.easyecommerce.entity.Product;
import com.fabrizio.easyecommerce.entity.User;
import com.fabrizio.easyecommerce.exception.Cart.CartAlreadyExistsException;
import com.fabrizio.easyecommerce.exception.Cart.CartNotFoundException;
import com.fabrizio.easyecommerce.exception.Products.ProductNotFoundException;
import com.fabrizio.easyecommerce.exception.quantity.QuantityException;
import com.fabrizio.easyecommerce.exception.user.UserNotFoundException;
import com.fabrizio.easyecommerce.mapper.CartItemMapper;
import com.fabrizio.easyecommerce.mapper.CartMapper;
import com.fabrizio.easyecommerce.repository.CartItemRepository;
import com.fabrizio.easyecommerce.repository.CartRepository;
import com.fabrizio.easyecommerce.repository.ProductRepository;
import com.fabrizio.easyecommerce.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j // Anotación de Lombok para habilitar el objeto 'log'
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private CartItemMapper cartItemMapper;

    @Override
    @Transactional
    public CartResponseDTO getMyCart() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Obteniendo carrito para el usuario: {}", email);

        Cart cart = cartRepository.findByUserEmail(email)
                .orElseGet(() -> {
                    log.warn("No se encontró carrito para {}, procediendo a crear uno nuevo", email);
                    User user = userRepository.findByEmail(email)
                            .orElseThrow(() -> {
                                log.error("Error al crear carrito: Usuario con email {} no existe", email);
                                return new UserNotFoundException("User with email " + email + " not found");
                            });
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.saveAndFlush(newCart);
                });

        return cartMapper.toCartResponseDTO(cart);
    }

    @Override
    @Transactional
    public void createCartForUser(Long userId) {
        log.info("Iniciando creación manual de carrito para el usuario con ID: {}", userId);

        User user = userRepository.findById(userId).orElseThrow(() -> {
            log.error("Fallo al crear carrito: ID de usuario {} no encontrado", userId);
            return new UserNotFoundException("User with id " + userId + " not found");
        });

        if (cartRepository.findByUserId(userId).isPresent()) {
            log.warn("El usuario {} ya posee un carrito activo", userId);
            throw new CartAlreadyExistsException("User with id " + userId + " already has a cart");
        }

        Cart newCart = new Cart();
        newCart.setUser(user);
        cartRepository.save(newCart);
        log.info("Carrito creado exitosamente para el usuario ID: {}", userId);
    }

    @Override
    @Transactional
    public void addItemToCart(Long userId, CartItemRequestDTO request) {
        log.info("Agregando producto ID: {} al carrito del usuario ID: {}", request.getProductId(), userId);

        Cart cart = cartRepository.findByUserId(userId).orElseThrow(() -> {
            log.error("Carrito no encontrado para el usuario ID: {}", userId);
            return new CartNotFoundException("Cart for user with id " + userId + " not found");
        });

        Optional<CartItem> itemExisted = cartItemRepository.findByCartIdAndProductId(cart.getId(), request.getProductId());

        if (itemExisted.isPresent()) {
            CartItem item = itemExisted.get();
            log.info("El producto ya existía en el carrito. Incrementando cantidad de {} a {}",
                    item.getQuantity(), item.getQuantity() + request.getQuantity());
            item.setQuantity(item.getQuantity() + request.getQuantity());
            cartItemRepository.save(item);
        } else {
            Product product = productRepository.findById(request.getProductId()).orElseThrow(() -> {
                log.error("Producto ID: {} no encontrado para agregar al carrito", request.getProductId());
                return new ProductNotFoundException("Product with id " + request.getProductId() + " not found");
            });

            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(request.getQuantity());
            newItem.setPriceAtPurchase(product.getPrice());
            cartItemRepository.save(newItem);
            log.info("Nuevo CartItem creado exitosamente para el producto: {}", product.getName());
        }
    }

    @Override
    @Transactional
    public void updateCartItemQuantity(Long cartItemId, int quantity) {
        log.info("Actualizando cantidad del CartItem ID: {} a {}", cartItemId, quantity);

        CartItem item = cartItemRepository.findById(cartItemId).orElseThrow(() -> {
            log.error("No se encontró el CartItem con ID: {}", cartItemId);
            return new CartNotFoundException("Cart item with id " + cartItemId + " not found");
        });

        if (quantity <= 0) {
            log.warn("Intento de asignar cantidad inválida ({}) al CartItem ID: {}", quantity, cartItemId);
            throw new QuantityException("Quantity must be greater than 0");
        }

        item.setQuantity(quantity);
        cartItemRepository.save(item);
    }

    @Override
    @Transactional
    public void removeCartItem(Long cartItemId) {
        log.info("Removiendo ítem ID: {} del carrito", cartItemId);

        if (!cartItemRepository.existsById(cartItemId)) {
            log.error("Fallo al eliminar: El CartItem ID: {} no existe", cartItemId);
            throw new CartNotFoundException("Cart item with id " + cartItemId + " not found");
        }
        cartItemRepository.deleteById(cartItemId);
        log.info("Ítem ID: {} eliminado correctamente", cartItemId);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        log.info("Vaciando carrito para el usuario con ID: {}", userId);

        Cart cart = cartRepository.findByUserId(userId).orElseThrow(() -> {
            log.error("No se puede vaciar: Carrito para usuario ID: {} no encontrado", userId);
            return new CartNotFoundException("Cart for user with id " + userId + " not found");
        });

        int itemsRemoved = cart.getItems().size();
        cart.getItems().clear();
        cartRepository.save(cart);
        log.info("Se eliminaron {} productos. El carrito del usuario {} ahora está vacío", itemsRemoved, userId);
    }
}