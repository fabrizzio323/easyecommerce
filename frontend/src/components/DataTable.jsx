export default function DataTable({ title, columns, rows, onNew, onEdit, onDelete }) {
    return (
        <div className="table-wrap">
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", padding: 14 }}>
                <strong style={{ fontSize: 15 }}>{title}</strong>
                <button className="btn" onClick={onNew}>+ Nuevo</button>
            </div>
            <table className="table">
                <thead>
                    <tr>
                        {columns.map((c) => <th key={c.key}>{c.label}</th>)}
                        <th style={{ textAlign: "right" }}>Acciones</th>
                    </tr>
                </thead>
                <tbody>
                    {rows.map((row) => (
                        <tr key={row.id}>
                            {columns.map((c) => (
                                <td key={c.key}>{c.render ? c.render(row) : row[c.key]}</td>
                            ))}
                            <td style={{ textAlign: "right" }}>
                                <button className="icon-btn" onClick={() => onEdit(row)} aria-label="Editar">✎</button>
                                <button className="icon-btn danger" onClick={() => onDelete(row)} aria-label="Eliminar">🗑</button>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
}