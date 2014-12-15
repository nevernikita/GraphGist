# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations


class Migration(migrations.Migration):

    dependencies = [
        ('PlotDistribution', '0004_nodeinoutdegree_nodepagerank'),
    ]

    operations = [
        migrations.CreateModel(
            name='InoutdegreeCount',
            fields=[
                ('id', models.AutoField(verbose_name='ID', serialize=False, auto_created=True, primary_key=True)),
                ('inoutdegree', models.IntegerField(default=0)),
                ('count', models.IntegerField(default=0)),
            ],
            options={
            },
            bases=(models.Model,),
        ),
        migrations.CreateModel(
            name='Node',
            fields=[
                ('nodeId', models.IntegerField(default=0, serialize=False, primary_key=True)),
                ('inoutdegree', models.IntegerField(default=0)),
                ('pagerank', models.FloatField(default=0)),
                ('radius', models.FloatField(default=0)),
                ('ev1', models.FloatField(default=0)),
                ('ev2', models.FloatField(default=0)),
            ],
            options={
            },
            bases=(models.Model,),
        ),
    ]
