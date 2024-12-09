import img1 from '../assets/1.jpg'
import img2 from '../assets/2.jpg'
import img3 from '../assets/3.jpg'
import { useState, useEffect } from 'react'
import api from '../services/api'
import { useNavigate } from 'react-router-dom'

const BikeOffer = () => {
    const [ebikes, setEbikes] = useState([]);
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    const navigate = useNavigate();

    const fetchBikes = async () => {
        try {
            const response = await api.get('/ebikes');
            console.log('API Response:', response.data);
            setEbikes(response.data);
        } catch (error) {
            console.error('Error fetching bikes:', error);
        }
    };

    useEffect(() => {
        const loggedIn = sessionStorage.getItem('userId') && sessionStorage.getItem('userRole');
        setIsLoggedIn(loggedIn);

        fetchBikes();
    }, []);

    const handleRent = (ebike) => {
        const customerName = sessionStorage.getItem('userName');
        const customerId = sessionStorage.getItem('userId');
        navigate(`/reserve/${ebike.id}`, {
            state: {
                ebikeId: ebike.id,
                ebikeName: ebike.model,
                customerName: customerName,
                customerId: customerId,
                pricePerDay: ebike.price
            }
        });
    };


    return (
        <div className="ebike-list">
            {ebikes.map((ebike) => (
                <div key={ebike.id} className="ebike-item">
                    <h3>{ebike.model}</h3>
                    <img src={getImage(ebike.id)} alt={ebike.model} />
                    <p>Battery Level: {ebike.batteryLevel}%</p>
                    <p>Price: {ebike.price}$ per day</p>
                    <p style={{ color: ebike.status === "Available" ? "green" : "red" }}>{ebike.status}</p>
                    <button onClick={() => handleRent(ebike)} disabled={ebike.status !== "Available"}>Rent Now</button>
                </div>
            ))}
        </div>
    );
}

const getImage = (id) => {
    switch (id) {
        case 1:
            return img1;
        case 2:
            return img2;
        case 3:
            return img3;
        default:
            return '';
    }
}
export default BikeOffer;