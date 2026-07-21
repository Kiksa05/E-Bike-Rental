import React, { useState } from 'react';
import './Reviews.css';

const Reviews = () => {
    const reviews = [
        {
            text: "I had an amazing off-road adventure with the Mountain Master. Thanks to EcoRide Rentals for the seamless service!",
            image: "https://images.pexels.com/photos/4946604/pexels-photo-4946604.jpeg?auto=compress&cs=tinysrgb&dpr=1&w=500",
            name: "Mila Krajnc",
            position: "Student"
        },
        {
            text: "The booking process was extremely easy and the vehicle was in perfect condition. I will definitely rent again!",
            image: "https://images.pexels.com/photos/220453/pexels-photo-220453.jpeg",
            name: "Marko Petrovski",
            position: "Traveler"
        },
        {
            text: "EcoRide Rentals made our weekend trip unforgettable. Great service, friendly staff, and amazing cars!",
            image: "https://images.pexels.com/photos/774909/pexels-photo-774909.jpeg",
            name: "Ana Dimitrova",
            position: "Designer"
        }
    ];

    const [currentReview, setCurrentReview] = useState(0);

    const nextReview = () => {
        setCurrentReview((prev) =>
            prev === reviews.length - 1 ? 0 : prev + 1
        );
    };

    const previousReview = () => {
        setCurrentReview((prev) =>
            prev === 0 ? reviews.length - 1 : prev - 1
        );
    };

    const review = reviews[currentReview];

    return (
        <section className="home-testimonial">
            <div className="container-fluid">

                <div className="row d-flex justify-content-center testimonial-pos">
                    <div className="col-md-12 pt-4 d-flex justify-content-center">
                        <h3>Testimonials</h3>
                    </div>
                </div>

                <section className="home-testimonial-bottom">
                    <div className="container testimonial-inner">

                        <button
                            className="review-arrow left"
                            onClick={previousReview}
                        >
                            ❮
                        </button>

                        <div className="row d-flex justify-content-center">
                            <div className="col-md-4 style-3">
                                <div className="tour-item">
                                    <div className="tour-desc bg-white">

                                        <div className="tour-text color-grey-3 text-center">
                                            &ldquo;{review.text}&rdquo;
                                        </div>

                                        <div className="d-flex justify-content-center pt-2 pb-2">
                                            <img
                                                className="tm-people"
                                                src={review.image}
                                                alt=""
                                            />
                                        </div>

                                        <div className="link-name d-flex justify-content-center">
                                            {review.name}
                                        </div>

                                        <div className="link-position d-flex justify-content-center">
                                            {review.position}
                                        </div>

                                    </div>
                                </div>
                            </div>
                        </div>

                        <button
                            className="review-arrow right"
                            onClick={nextReview}
                        >
                            ❯
                        </button>

                    </div>
                </section>

            </div>
        </section>
    );
}

export default Reviews;