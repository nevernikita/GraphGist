# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations


class Migration(migrations.Migration):

    dependencies = [
        ('PlotDistribution', '0003_auto_20141106_2248'),
    ]

    operations = [
        migrations.CreateModel(
            name='NodeInoutdegree',
            fields=[
                ('id', models.AutoField(verbose_name='ID', serialize=False, auto_created=True, primary_key=True)),
                ('node', models.IntegerField(default=0)),
                ('inoutdegree', models.IntegerField(default=0)),
            ],
            options={
            },
            bases=(models.Model,),
        ),
        migrations.CreateModel(
            name='NodePagerank',
            fields=[
                ('id', models.AutoField(verbose_name='ID', serialize=False, auto_created=True, primary_key=True)),
                ('node', models.IntegerField(default=0)),
                ('pagerank', models.FloatField(default=0)),
            ],
            options={
            },
            bases=(models.Model,),
        ),
    ]
