document.addEventListener("DOMContentLoaded", function () {
    const urlParams = new URLSearchParams(window.location.search);

    async function confirm() {
        let requestData = {
            paymentKey: urlParams.get("paymentKey"),
            orderId: urlParams.get("orderId"),
            amount: urlParams.get("amount"),
        };

        const response = await fetch("/confirm", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify(requestData),
        });

        const json = await response.json();


        if (!response.ok) {
            window.location.href = `/fail?message=${json.message}&code=${json.code}`;
        }
    }
    confirm();
});