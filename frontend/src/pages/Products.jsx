import { useEffect, useState } from "react";
import { getProducts } from "../api/productService";
import { getCategories } from "../api/categoryService";
import ProductCard from "../components/ProductCard";
import ProductSkeleton from "../components/ProductSkeleton";
import CategoryChips from "../components/CategoryChips";

export default function Products() {
    const [products, setProducts] = useState([]);
    const [categories, setCategories] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [search, setSearch] = useState("");
    const [debouncedSearch, setDebouncedSearch] = useState("");
    const [selectedCategory, setSelectedCategory] = useState(null);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(1);
    const [totalElements, setTotalElements] = useState(0);

    useEffect(() => {
        const timer = setTimeout(() => setDebouncedSearch(search.trim()), 350);
        return () => clearTimeout(timer);
    }, [search]);

    useEffect(() => {
        let cancelled = false;
        getCategories()
            .then(({ data }) => {
                if (!cancelled) setCategories(Array.isArray(data) ? data : (data?.content ?? []));
            })
            .catch((err) => console.error("Error al cargar categorías", err));
        return () => { cancelled = true; };
    }, []);

    useEffect(() => {
        let cancelled = false;
        async function loadProducts() {
            setLoading(true);
            setError("");
            try {
                const { data } = await getProducts(page, 20, selectedCategory, debouncedSearch);
                if (cancelled) return;
                const list = Array.isArray(data) ? data : (data?.content ?? []);
                setProducts(list);
                setTotalPages(Array.isArray(data) ? 1 : (data?.totalPages ?? 1));
                setTotalElements(Array.isArray(data) ? list.length : (data?.totalElements ?? list.length));
            } catch (err) {
                if (cancelled) return;
                console.error("Error al cargar productos", err);
                setProducts([]);
                setTotalPages(1);
                setTotalElements(0);
                setError("No se pudieron cargar los productos.");
            } finally {
                if (!cancelled) setLoading(false);
            }
        }
        loadProducts();
        return () => { cancelled = true; };
    }, [page, selectedCategory, debouncedSearch]);

    const selectCategory = (id) => {
        setSelectedCategory(id);
        setPage(0);
    };

    return (
        <div>
            <div className="page-header">
                <div>
                    <h1>Productos</h1>
                    {!loading && !error && (
                        <p>{totalElements} productos encontrados (Página {page + 1} de {totalPages})</p>
                    )}
                </div>
            </div>

            <div style={{ margin: "20px 0", display: "flex", gap: 12, flexWrap: "wrap" }}>
                <input
                    type="search"
                    placeholder="Buscar producto..."
                    value={search}
                    onChange={(e) => { setSearch(e.target.value); setPage(0); }}
                    style={{ flex: "1 1 240px", maxWidth: 360 }}
                />
                <CategoryChips
                    categories={categories}
                    selected={selectedCategory}
                    onSelect={selectCategory}
                />
            </div>

            {loading && (
                <div className="product-grid">
                    {Array.from({ length: 6 }).map((_, i) => <ProductSkeleton key={i} />)}
                </div>
            )}

            {error && (
                <div className="empty-state">
                    <p>{error}</p>
                    <p style={{ fontSize: 13, color: "var(--muted)" }}>
                        Revisá la conexión con el backend o tu sesión de usuario.
                    </p>
                </div>
            )}

            {!loading && !error && products.length === 0 && (
                <div className="empty-state"><p>No se encontraron productos.</p></div>
            )}

            {!loading && !error && products.length > 0 && (
                <div className="product-grid">
                    {products.map((product) => <ProductCard key={product.id} product={product} />)}
                </div>
            )}

            {!loading && !error && totalPages > 1 && (
                <div style={{ display: "flex", justifyContent: "center", alignItems: "center", gap: 16, margin: "40px 0 20px" }}>
                    <button
                        className="btn secondary"
                        disabled={page === 0}
                        onClick={() => {
                            setPage((prev) => Math.max(0, prev - 1));
                            window.scrollTo({ top: 0, behavior: "smooth" });
                        }}
                        style={{ cursor: page === 0 ? "not-allowed" : "pointer", opacity: page === 0 ? 0.5 : 1 }}
                    >← Anterior</button>
                    <span style={{ fontSize: 14, color: "var(--muted)", fontWeight: 500 }}>
                        Página {page + 1} de {totalPages}
                    </span>
                    <button
                        className="btn secondary"
                        disabled={page >= totalPages - 1}
                        onClick={() => {
                            setPage((prev) => Math.min(totalPages - 1, prev + 1));
                            window.scrollTo({ top: 0, behavior: "smooth" });
                        }}
                        style={{ cursor: page >= totalPages - 1 ? "not-allowed" : "pointer", opacity: page >= totalPages - 1 ? 0.5 : 1 }}
                    >Siguiente →</button>
                </div>
            )}
        </div>
    );
}
