# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations


class Migration(migrations.Migration):

    dependencies = [
        ('PlotDistribution', '0007_auto_20141208_0858'),
    ]

    operations = [
        migrations.AlterField(
            model_name='node',
            name='ev1',
            field=models.CharField(default=0, max_length=30, db_index=True),
            preserve_default=True,
        ),
        migrations.AlterField(
            model_name='node',
            name='ev2',
            field=models.CharField(default=0, max_length=30, db_index=True),
            preserve_default=True,
        ),
        migrations.AlterField(
            model_name='node',
            name='ev3',
            field=models.CharField(default=0, max_length=30, db_index=True),
            preserve_default=True,
        ),
        migrations.AlterField(
            model_name='node',
            name='pagerank',
            field=models.CharField(default=0, max_length=30, db_index=True),
            preserve_default=True,
        ),
    ]
