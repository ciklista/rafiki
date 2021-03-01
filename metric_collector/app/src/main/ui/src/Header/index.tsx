import React from 'react';
import { Link } from 'react-router-dom'

function Header(): JSX.Element {
    return (
        <nav>
            <div className="shadow-md bg-green-300 p-4 rounded-b">
                <div className="no-underline text-gray-500">
                    <Link to="/">Rafiki</Link>
                </div>
            </div>
        </nav>
    );
}

export default Header;