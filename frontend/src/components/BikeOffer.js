import img1 from '../assets/1.jpg'
import img2 from '../assets/2.jpg'
import img3 from '../assets/3.jpg'
import { useState, useEffect } from 'react'
import api from '../services/api'
import { useNavigate } from 'react-router-dom'

const BatteryGauge = ({ level }) => {
    const filled = Math.round(level / 10);
    return (
        <div className={level < 30 ? 'battery low' : 'battery'}>
            <div className="battery-cells" aria-hidden="true">
                {Array.from({ length: 10 }, (_, i) => (
                    <span key={i} className={i < filled ? 'on' : ''} />
                ))}
            </div>
            <span>{level}% battery</span>
        </div>
    );
};

const BikeOffer = () => {
    const [ebikes, setEbikes] = useState(null);
    const [loadFailed, setLoadFailed] = useState(false);
    const navigate = useNavigate();

    useEffect(() => {
        api.get('/ebikes')
            .then((response) => setEbikes(response.data))
            .catch((error) => {
                console.error('Error fetching bikes:', error);
                setLoadFailed(true);
            });
    }, []);

    const handleRent = (ebike) => {
        if (!sessionStorage.getItem('token')) {
            navigate('/login', { state: { message: 'Log in to rent a bike.' } });
            return;
        }
        navigate(`/reserve/${ebike.id}`, {
            state: {
                ebikeId: ebike.id,
                ebikeName: ebike.model,
                pricePerDay: ebike.price
            }
        });
    };

    return (
        <section className="bikes" id="bikes">
            <h2>Choose your bike</h2>
            {loadFailed && <p className="bikes-note">The bikes could not be loaded. Refresh the page to try again.</p>}
            {ebikes?.length === 0 && <p className="bikes-note">No bikes are listed right now. Check back soon.</p>}
            <div className="ebike-list">
                {ebikes?.map((ebike) => {
                    const available = ebike.status === 'Available';
                    const image = getImage(ebike.id);
                    return (
                        <article key={ebike.id} className={available ? 'ebike-item' : 'ebike-item unavailable'}>
                            <div className="ebike-photo">
                                {image ? <img src={image} alt={ebike.model} /> : <span>No photo yet</span>}
                            </div>
                            <div className="ebike-details">
                                <h3>{ebike.model}</h3>
                                <p className="ebike-price"><strong>${ebike.price}</strong> per day</p>
                                <BatteryGauge level={ebike.batteryLevel} />
                                <button
                                    className="auth-button"
                                    aria-label={available ? `Rent ${ebike.model}` : undefined}
                                    onClick={() => handleRent(ebike)}
                                    disabled={!available}>
                                    {available ? 'Rent this bike' : 'Not available'}
                                </button>
                            </div>
                        </article>
                    );
                })}
            </div>
        </section>
    );
}

export const getImage = (id) => {
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
