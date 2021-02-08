import React from 'react';
import './Header.css';
import { Link } from 'react-router-dom'

function Header(): JSX.Element {
    return (
        <nav>
            <div className="header-body">
                <div className="header-content">
                    <Link to="/">Rafiki</Link>
                </div>
            </div>
        </nav>
    );
}

export default Header;