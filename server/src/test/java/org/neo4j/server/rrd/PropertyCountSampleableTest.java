/**
 * Copyright (c) 2002-2012 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.server.rrd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.server.database.Database;
import org.neo4j.server.rrd.sampler.PropertyCountSampleable;
import org.neo4j.test.ImpermanentGraphDatabase;

public class PropertyCountSampleableTest
{
    public Database db;
    public PropertyCountSampleable sampleable;

    @Test
    public void emptyDbHasZeroNodesInUse()
    {
        assertThat( sampleable.getValue(), is( 0d ) );
    }

    @Test
    public void addANodeAndSampleableGoesUp()
    {
        addPropertyToReferenceNode();

        assertThat( sampleable.getValue(), is( 1d ) );

        addNodeIntoGraph();
        addNodeIntoGraph();

        assertThat( sampleable.getValue(), is( 3d ) );
    }

    private void addNodeIntoGraph()
    {
        Transaction tx = db.graph.beginTx();
        Node referenceNode = db.graph.getReferenceNode();
        Node myNewNode = db.graph.createNode();
        myNewNode.setProperty( "id", UUID.randomUUID().toString() );
        myNewNode.createRelationshipTo( referenceNode, new RelationshipType()
        {
            public String name()
            {
                return "knows_about";
            }
        } );

        tx.success();
        tx.finish();
    }

    private void addPropertyToReferenceNode()
    {
        Transaction tx = db.graph.beginTx();
        Node n = db.graph.getReferenceNode();
        n.setProperty( "monkey", "rock!" );
        tx.success();
        tx.finish();
    }

    @Before
    public void setUp() throws Exception
    {
        db = new Database( new ImpermanentGraphDatabase() );
        sampleable = new PropertyCountSampleable( db.graph );
    }

    @After
    public void shutdownDatabase()
    {
        db.shutdown();
    }
}
