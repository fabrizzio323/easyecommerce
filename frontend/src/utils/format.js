export const money = (n) => "$ " + Number(n || 0).toLocaleString("es-AR");

export const dateTime = (value) =>
    value ? new Date(value).toLocaleDateString("es-AR", { day: "2-digit", month: "short", year: "numeric" }) : "";