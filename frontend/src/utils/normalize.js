const normalizeItem = (it) => {
    const product = it.product ?? {};
    const price = Number(it.priceAtPurchase ?? it.unitPrice ?? it.price ?? product.price ?? 0);
    const quantity = Number(it.quantity ?? 1);
    const subtotal = Number(it.subtotal ?? (price * quantity));
    return {
        id: it.id,
        productId: product.id ?? it.productId,
        name: it.productName ?? product.name ?? it.name ?? "Producto",
        imageUrl: product.imageUrl ?? it.imageUrl ?? null,
        price,
        quantity,
        subtotal,
    };
};

export function normalizeCart(data) {
    const raw = Array.isArray(data) ? data : data?.items ?? [];
    const items = raw.map(normalizeItem);
    const total = Number(data?.total ?? data?.totalPrice ?? items.reduce((s, i) => s + i.subtotal, 0));
    return { items, total };
}

export function normalizeOrder(o) {
    const raw = o.items ?? o.orderItems ?? [];
    const items = raw.map(normalizeItem);
    const total = Number(o.totalPrice ?? o.total ?? items.reduce((s, i) => s + i.subtotal, 0));
    return {
        id: o.id,
        userEmail: o.userEmail ?? "",
        status: o.status ?? "CREATED",
        createdAt: o.createdAt ?? o.date ?? o.orderDate,
        shippingAddress: o.shippingAddress ?? "",
        city: o.city ?? "",
        zipCode: o.zipCode ?? "",
        total,
        items,
    };
}
