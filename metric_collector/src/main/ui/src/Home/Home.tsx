import axios from 'axios';
import Server from '../models/Server';
import React, { useEffect, useState } from 'react';
import './Home.css';

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
                <div className={`server status-${server.status}`} key={server.id}>
                    <div>{server.name}</div>
                    <div>{server.ip_adress}</div>
                </div>
            )}
        </div>
    )
}