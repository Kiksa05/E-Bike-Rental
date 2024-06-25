import React from 'react';
import './Reviews.css';

const Reviews = () => {
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
                        <div className="row d-flex justify-content-center">
                            <div className="col-md-4 style-3">
                                <div className="tour-item">
                                    <div className="tour-desc bg-white">
                                        <div className="tour-text color-grey-3 text-center">
                                            &ldquo;I had an amazing off-road adventure with the Mountain Master. Thanks to EcoRide Rentals for the seamless service!&rdquo;
                                        </div>
                                        <div className="d-flex justify-content-center pt-2 pb-2">
                                            <img className="tm-people" src="https://images.pexels.com/photos/4946604/pexels-photo-4946604.jpeg?auto=compress&cs=tinysrgb&dpr=1&w=500" alt="" />
                                        </div>
                                        <div className="link-name d-flex justify-content-center">Mila Krajnc</div>
                                        <div className="link-position d-flex justify-content-center">Student</div>
                                    </div>
                                </div>
                            </div>

                        </div>
                    </div>
                </section>
            </div>
        </section>
    );
}

export default Reviews;
