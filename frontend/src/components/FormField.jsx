export default function FormField({ label, ...props }) {
    return (
        <div className="field">
            <label>{label}</label>
            <input className="input" {...props} />
        </div>
    );
}