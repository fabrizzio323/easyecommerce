export default function ProductSkeleton() {
    return (
        <div className="skeleton">
            <div className="image" />
            <div style={{ padding: 14 }}>
                <div className="line" style={{ width: "30%" }} />
                <div className="line" style={{ width: "80%" }} />
                <div className="line" style={{ width: "40%", marginBottom: 0 }} />
            </div>
        </div>
    );
}