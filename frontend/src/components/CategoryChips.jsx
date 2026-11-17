export default function CategoryChips({ categories, selected, onSelect }) {
    return (
        <div style={{ display: "flex", gap: 8, flexWrap: "wrap" }}>
            <button className={`chip ${!selected ? "active" : ""}`} onClick={() => onSelect(null)}>
                Todas
            </button>
            {categories.map((c) => (
                <button
                    key={c.id}
                    className={`chip ${selected === c.id ? "active" : ""}`}
                    onClick={() => onSelect(c.id)}
                >
                    {c.name}
                </button>
            ))}
        </div>
    );
}