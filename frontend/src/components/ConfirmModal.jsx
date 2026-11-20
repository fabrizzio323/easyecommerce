export default function ConfirmModal({ title, message, onConfirm, onCancel }) {
    return (
        <div className="overlay" onClick={onCancel}>
            <div className="modal" onClick={(e) => e.stopPropagation()}>
                <h3>{title}</h3>
                <p>{message}</p>
                <div className="actions">
                    <button className="btn secondary" onClick={onCancel}>Cancelar</button>
                    <button className="btn danger" onClick={onConfirm}>Eliminar</button>
                </div>
            </div>
        </div>
    );
}