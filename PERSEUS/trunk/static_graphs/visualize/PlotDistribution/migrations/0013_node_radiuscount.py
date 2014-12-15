# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations


class Migration(migrations.Migration):

    dependencies = [
        ('PlotDistribution', '0012_auto_20141209_1944'),
    ]

    operations = [
        migrations.CreateModel(
            name='Node',
            fields=[
                ('nodeId', models.IntegerField(default=0, serialize=False, primary_key=True)),
                ('inoutdegree', models.IntegerField(default=0, db_index=True)),
                ('pagerank', models.CharField(default=0, max_length=30, db_index=True)),
                ('radius', models.CharField(default=0, max_length=30, db_index=True)),
                ('ev1', models.CharField(default=0, max_length=30, db_index=True)),
                ('ev2', models.CharField(default=0, max_length=30, db_index=True)),
                ('ev3', models.CharField(default=0, max_length=30, db_index=True)),
            ],
            options={
            },
            bases=(models.Model,),
        ),
        migrations.CreateModel(
            name='RadiusCount',
            fields=[
                ('id', models.AutoField(verbose_name='ID', serialize=False, auto_created=True, primary_key=True)),
                ('radius', models.CharField(default=0, max_length=30, db_index=True)),
                ('count', models.IntegerField(default=0, db_index=True)),
            ],
            options={
            },
            bases=(models.Model,),
        ),
    ]
