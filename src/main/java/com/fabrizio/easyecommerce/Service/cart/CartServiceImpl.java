package com.fabrizio.easyecommerce.Service.cart;
import lombok.RequiredArgsConstructor;

import com.fabrizio.easyecommerce.dto.cart.CartItemRequestDTO;
import com.fabrizio.easyecommerce.dto.cart.CartResponseDTO;
import com.fabrizio.easyecommerce.entity.Cart;
import com.fabrizio.easyecommerce.entity.CartItem;
import com.fabrizio.easyecommerce.entity.Product;
import com.fabrizio.easyecommerce.entity.User;
import com.fabrizio.easyecommerce.exception.Cart.CartAlreadyExistsException;
import com.fabrizio.easyecommerce.exception.Cart.CartNotFoundException;
import com.fabrizio.easyecommerce.exception.Products.ProductNotFoundException;
import com.fabrizio.easyecommerce.exception.quantity.QuantityException;
import com.fabrizio.easyecommerce.exception.user.UserNotFoundException;
import com.fabrizio.easyecommerce.mapper.Cart.CartItemMapper;
import com.fabrizio.easyecommerce.mapper.Cart.CartMapper;
import com.fabrizio.easyecommerce.repository.Cart.CartItemRepository;
import com.fabrizio.easyecommerce.repository.Cart.CartRepository;
import com.fabrizio.easyecommerce.repository.product.ProductRepository;
import com.fabrizio.easyecommerce.repository.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j // Anotación de Lombok para habilitar el objeto 'log'
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CartMapper cartMapper;
    private final CartItemMapper cartItemMapper;

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
        String currentEmail = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        User currentUser = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new UserNotFoundException(
                        "User with email " + currentEmail + " not found"
                ));

        if (!currentUser.getId().equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "No tienes autorización para modificar el carrito de otro usuario"
            );
        }
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
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Usuario [{}] actualizando cantidad del CartItem ID: {} a {}", currentEmail, cartItemId, quantity);

        CartItem item = cartItemRepository.findById(cartItemId).orElseThrow(() -> {
            log.error("No se encontró el CartItem con ID: {}", cartItemId);
            return new CartNotFoundException("Cart item with id " + cartItemId + " not found");
        });

        // 🔒 CONTROL DE ACCESO A NIVEL DE OBJETO (PROPIEDAD):
        if (!item.getCart().getUser().getEmail().equals(currentEmail)) {
            log.warn("ACCESO DENEGADO: El usuario [{}] intentó modificar el CartItem [{}] perteneciente a [{}]",
                    currentEmail, cartItemId, item.getCart().getUser().getEmail());
            throw new org.springframework.security.access.AccessDeniedException(
                    "No tienes autorización para modificar ítems de otro usuario");
        }

        if (quantity <= 0) {
            log.warn("Intento de asignar cantidad inválida ({}) al CartItem ID: {}", quantity, cartItemId);
            throw new QuantityException("Quantity must be greater than 0");
        }

        item.setQuantity(quantity);
        cartItemRepository.save(item);
        log.info("CartItem ID: {} actualizado exitosamente a cantidad {}", cartItemId, quantity);
    }

    @Override
    @Transactional
    public void removeCartItem(Long cartItemId) {
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Usuario [{}] removiendo ítem ID: {} del carrito", currentEmail, cartItemId);

        CartItem item = cartItemRepository.findById(cartItemId).orElseThrow(() -> {
            log.error("Fallo al eliminar: El CartItem ID: {} no existe", cartItemId);
            return new CartNotFoundException("Cart item with id " + cartItemId + " not found");
        });

        // 🔒 CONTROL DE ACCESO A NIVEL DE OBJETO (PROPIEDAD):
        if (!item.getCart().getUser().getEmail().equals(currentEmail)) {
            log.warn("ACCESO DENEGADO: El usuario [{}] intentó eliminar el CartItem [{}] perteneciente a [{}]",
                    currentEmail, cartItemId, item.getCart().getUser().getEmail());
            throw new org.springframework.security.access.AccessDeniedException(
                    "No tienes autorización para eliminar ítems de otro usuario");
        }

        cartItemRepository.delete(item);
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

    @Override
    @Transactional
    public void clearCartByEmail(String email) {
        log.info("Vaciando carrito para el usuario autenticado con email: {}", email);

        Cart cart = cartRepository.findByUserEmail(email).orElseThrow(() -> {
            log.error("No se puede vaciar: Carrito para usuario {} no encontrado", email);
            return new CartNotFoundException("Cart for user " + email + " not found");
        });

        int itemsRemoved = cart.getItems().size();
        cart.getItems().clear();
        cartRepository.save(cart);
        log.info("Se eliminaron {} productos. El carrito del usuario {} ahora está vacío", itemsRemoved, email);
    }
}