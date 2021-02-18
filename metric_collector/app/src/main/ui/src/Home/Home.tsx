import axios from 'axios';
import Server from '../models/Server';
import React, { useEffect, useState } from 'react';
import './Home.css';
import { Link } from 'react-router-dom';

export default function Home() {

    const [servers, setServers] = useState([]);
    useEffect(() => {
        axios.get('http://localhost:3000/servers')
            .then(response => setServers(response.data))
            .catch(error => console.log(error))
    }, [])

    return (
        <div className="content">
            {servers.map((server: Server) =>
                <Link to={`/server/${server.id}`} className={`server status-${server.status}`} key={server.id}>
                    <div>{server.name}</div>
                    <div>{server.ip_adress}</div>
                </Link>
            )}
        </div>
    )
}