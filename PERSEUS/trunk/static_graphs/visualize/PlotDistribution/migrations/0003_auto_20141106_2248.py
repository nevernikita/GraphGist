# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations


class Migration(migrations.Migration):

    dependencies = [
        ('PlotDistribution', '0002_auto_20141106_2248'),
    ]

    operations = [
        migrations.AlterField(
            model_name='edge',
            name='fromNode',
            field=models.IntegerField(default=0, db_index=True),
            preserve_default=True,
        ),
        migrations.AlterField(
            model_name='edge',
            name='toNode',
            field=models.IntegerField(default=0, db_index=True),
            preserve_default=True,
        ),
    ]
